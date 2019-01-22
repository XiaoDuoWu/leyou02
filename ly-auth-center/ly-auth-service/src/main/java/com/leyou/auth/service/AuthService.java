package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.pojo.User;
import com.leyou.userinterface.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private UserClient userClient;

    public String login(String username, String password) {
        try {
//        调用UserClient接口
            User user = userClient.queryUserByUsernameAndPassword(username, password);
//       判断是否为空
            if (user == null) {
                throw new LyException(ExceptionEnum.INVALID_UN_OR_PW);
            }
//        生成userInfo
            UserInfo userInfo = new UserInfo(user.getId(), user.getUsername());
//        生成token
            String token = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), prop.getExpire());
            return  token;
        } catch (LyException e) {
            throw new LyException(ExceptionEnum.INVALID_UN_OR_PW);
        }
    }
}
