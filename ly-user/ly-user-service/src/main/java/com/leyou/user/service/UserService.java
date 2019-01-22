package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.pojo.User;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.utils.CodecUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:phone:";
    public Boolean checkData(String data, Integer type) {
        User user = new User();
//        根据type选择传入的类型
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
                default:
                    throw new LyException(ExceptionEnum.BAD_REQUEST);
        }
//        有返回false 没有返回true
        return  userMapper.selectCount(user) == 0 ;
    }

    public void sendCode(String phone) {
        // 1 验证手机号
        String regex = "^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\\d{8}$";
        if(!phone.matches(regex)){
            throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        // 生成随机的验证码
        String code = CodecUtils.generateCode(6);
        //发送验证码 并发送到rabbitmq  sms处理接收的消息
        Map<String, String> map = new HashMap<>();
        map.put("phone", phone);
        map.put("code", code);
        amqpTemplate.convertAndSend("ly.sms.exchange", "register.verify.code", map);
//        存进redis中 方便进行验证 并设置过期时间 5分钟
        redisTemplate.opsForValue().set(KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);

    }


    //    用户注册
    public void register(User user, String code) {
        // TODO 校验用户数据

        // 校验验证码   取出缓存验证码
        String cacheCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(cacheCode, code)) {
            throw new LyException(ExceptionEnum.DATA_TYPE_ERROR);
        }
        String salt = CodecUtils.generateSalt();
        // 对密码进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        // 存入数据库
        user.setCreated(new Date());
        user.setSalt(salt);
        userMapper.insert(user);
    }

    public User queryUserByUserNameAndPassword(String username, String password) {
//        先查询用户名是否存在  不存在 不判断密码是否正确
        User user = new User();
        user.setUsername(username);
        User user1 = userMapper.selectOne(user);
        if (user1 == null) {
            throw new LyException(ExceptionEnum.INVALID_UN_OR_PW);
        }
        String salt = user1.getSalt();
//        判断密码是否正确
        String pw = CodecUtils.md5Hex(password, salt);
        if (!StringUtils.equals(pw, user1.getPassword())) {
            throw new LyException(ExceptionEnum.INVALID_UN_OR_PW);
        }
//        账号密码都正确 正常返回user对象
        return user1;
    }

}