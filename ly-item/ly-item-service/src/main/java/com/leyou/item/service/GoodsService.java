package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        // 1 分页
        PageHelper.startPage(page, rows);

        // 2 过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 2.1 模糊搜索
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 2.2 上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // 2.3 逻辑删除过滤
        criteria.andEqualTo("valid", true);

        // 3 默认排序
        example.setOrderByClause("last_update_time DESC");

        // 4 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        // 5 处理分类和品牌的名称
        handleCategoryAndBrandName(spus);

        // 6 返回
        PageInfo<Spu> info = new PageInfo<>(spus);
        return new PageResult<>(info.getTotal(), spus);
    }

    private void handleCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
/*             Brand brand = brandService.queryBrandById(spu.getBrandId());
            spu.setBname(brand.getName());
            String cname1 = categoryService.queryCategoryByid(spu.getCid1()).getName();
            //System.out.println("cname1 = " + cname1);
            String cname2 = categoryService.queryCategoryByid(spu.getCid2()).getName();
            //System.out.println("cname2 = " + cname2);
            String cname3 = categoryService.queryCategoryByid(spu.getCid3()).getName();
            //System.out.println("cname3 = " + cname3);
            spu.setCname(cname1 + "/" + cname2 + "/" + cname3);*/

            //处理分类和品牌的名称
            /**
             * 思路: 查询商品所属分类，有三级分类。可以根据三级分类的ID查三级的分类名称
             * 可以有两种方法：1.分别查出每一级分类的名称进行拼接，但是这样会多次查询数据库
             * 我们希望一次访问数据库就能查出需要的数据
             * 2.把3种级别的ID封装成list，查询出分类的集合，通用IdListMapper有直接根据list查的方法
             */
            List<Category> categoryList = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
            //为了获取list中的每个分类的名称，可以通过Stream流(详见day13笔记)  Sream流：解决集合的代码冗余弊端.
            String names = categoryList
                    .stream()//把集合转成Stream流
                    .map(Category::getName)//把list流映射成name流  通过对象名引用成员方法
                    .collect(Collectors.joining("/"));//把结果收集到集合中
            //设置分类名称
            spu.setCname(names);
            Brand brand = brandService.queryBrandById(spu.getBrandId());
            //设置品牌名称
            spu.setBname(brand.getName());
        }
    }

    public void saveGoods(Spu spu) {

        //新增spu-----
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);//设置为false代表没有被逻辑删除
        int count = 0;
        count = spuMapper.insert(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
        }

        //新增SpuDetail--------
        SpuDetail spuDetail = spu.getSpuDetail();
        //只要设置个id属性，其他都已存在（description-第二页、specialSpec-第四页、 genericSpec-第三页 packingList、afterService-第一页）
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        saveSkuAndStock(spu);
//        发送消息 给rabbit
        sendMessage(spu.getId(),"insert");

    }

    private void saveSkuAndStock(Spu spu) {
        int count;//新增Sku表------
        List<Sku> skus = spu.getSkus();
        //new 一个集合存放stock
        ArrayList<Stock> stockList = new ArrayList<>();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
            }
            Stock stock = new Stock();
            stock.setStock(sku.getStock());
            stock.setSkuId(sku.getId());
            //把stock放到集合中
            stockList.add(stock);
        }
        //通过InsertList批量增加 XXXXXXXX 不可以批量新增，因为批量新增stock的id还没有，为null，因为sku的id 还是为null,只能单个新增
        //int i = skuMapper.insertList(skus);

        //新增库存Stock表----可以批量新增，因为该有 的数据都有了
        count = stockMapper.insertList(stockList);
        if (count != stockList.size()) {
            throw new LyException(ExceptionEnum.GOODS_INSERT_ERROR);
        }
    }

    public void updateSaleable(Long id, Boolean saleable) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);
        //记住有最后更新事件的 一定要更新时间
        spu.setLastUpdateTime(new Date());
        int i = spuMapper.updateByPrimaryKeySelective(spu);
        if (i != 1) {
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
    }

    public SpuDetail queryBySpuId(Long spu_id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spu_id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return spuDetail;
    }

    //为了页面回显方便，我们一并把sku的库存stock也查询出来
    public List<Sku> querySkuBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //把skus映射成ids
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        //根据ids用selectByIdList查stocks
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //把list映射成map，key是id，value是库存值
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        for (Sku skus1 : skus) {
            //根据skus1的id查map中对应的库存设置到skus1中
            skus1.setStock(map.get(skus1.getId()));
        }
        return skus;
    }

    /**
     * 修改商品：
     *
     *      2.需要删除对应的sku及库存，然后把新的值插入（因为sku的属性可能有增加或者减少，比如可能增加新的机身颜色，改起来不方便）
     *          2.1、删除sku表需要通过spu_id
     *          2.2、删除stock需要sku_id,如何获取sku_id，先查后删
     *      3.处理不需要的字段(更新时间，vaild、saleavle、创建时间)
     * @param spu
     */
    @Transactional
    public void updateGoods(Spu spu) {
        int count = 0;
        //获取spu_id
        Long id = spu.getId();
        if (id==null){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        Sku sku = new Sku();
        sku.setSpuId(id);
        //根据spu_id查skus
        List<Sku> skus = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //将skus映射成ids
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        //根据ids删stock
        count = stockMapper.deleteByIdList(ids);
        if (count!=skus.size()){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //根据spu_id删sku
        count = skuMapper.delete(sku);
        if (count!=skus.size()){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //设置spu的更新时间，Selective--如果为Null就忽略更新
        spu.setLastUpdateTime(new Date());
        spu.setSaleable(null);
        spu.setValid(null);
        spu.setCreateTime(null);
        //修改spu表
        count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count!=1){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //修改spuDetail表
        count = spuDetailMapper.updateByPrimaryKey(spu.getSpuDetail());
        if (count!=1){
            throw new LyException(ExceptionEnum.GOODS_EDIT_ERROR);
        }
        //新增sku和stock---之前做过相同的业务，方法抽取出来调用即可.
        saveSkuAndStock(spu);
        sendMessage(spu.getId(),"update");
    }
//根据spuId 查询spu
    public Spu querySpuById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 查询spuDetail
        spu.setSpuDetail(queryBySpuId(id));
        // 查询skus
        spu.setSkus(querySkuBySpuId(id));
        return spu;
    }


//    rabbit
private void sendMessage(Long id, String type){
    // 发送消息
    try {
        this.amqpTemplate.convertAndSend("item." + type, id);
    } catch (Exception e) {
        log.error("{}商品消息发送异常，商品id：{}", type, id, e);
    }
}

    public List<Sku> querySkuByIds(List<Long> ids) {
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 填充库存
        fillStock(ids, skus);
        return skus;

    }

    private void fillStock(List<Long> ids, List<Sku> skus) {
//         查询库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
//        把库存转为map  key是skuId  value是库存  转为map的原因是 map的key是skuId map的value是skuId的stock值
        Map<Long, Integer> map = stocks.stream().collect(Collectors.toMap(s -> s.getSkuId(), s -> s.getStock()));
//        保存在sku
        for (Sku sku : skus) {
//            把库存的数量保存在sku中
            sku.setStock(map.get(sku.getId()));
        }


    }

    @Transactional
    public void decreaseStock(List<CartDTO> cartDTOS) {
        for (CartDTO cartDTO : cartDTOS) {
            int count = stockMapper.decreaseStock(cartDTO.getSkuId(), cartDTO.getNum());
            if(count != 1){
                throw new RuntimeException("库存不足");
            }
        }
    }
}
