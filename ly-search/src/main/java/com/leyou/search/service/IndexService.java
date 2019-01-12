package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;

    public Goods buildGoods(Spu spu) throws Exception {
//    查询分类
        String categoryNames = categoryClient.queryListByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())).stream().map(Category::getName).collect(Collectors.joining(""));
//    查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
//    查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
//    查询商品详情
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
//    搜索字段all
        String all = spu.getTitle() + categoryNames + brand.getName();
//    创建集合 处理sku
        List<Map<String, Object>> skus = new ArrayList<>();
//    价格集合
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
//        把sks的处理成map的属性添加进map中
            skus.add(map);
        }
        // 查询规格参数
        List<SpecParam> params = specClient.queryParamByGid(null, spu.getCid3(), true);
        Set<Long> price = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());
//获取通用规格参数
        Map<Long, String> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        // 获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils
                .nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
                });
        // 规格参数,key是规格参数的名字,值是规格参数的值
        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            // 规格名称
            String key = param.getName();
            Object value = "";
            // 判断是否是通用规格
            if (param.getGeneric()) {
                value = genericSpec.get(param.getId());
                // 判断是否是数值类型
                if (param.getNumeric()) {
                    // 处理成段
                    value = chooseSegment(value.toString(), param);
                }
            } else {
                value = specialSpec.get(param.getId());
            }
            value = value == null ? "其他" : value;
            // 存入map
            specs.put(key, value);
        }

        // 构建goods对象
        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime().getTime());
        goods.setId(spu.getId());
        goods.setAll(all);// 搜索字段,包含标题,分类,品牌,规格等
        goods.setPrice(price);// 所有sku的价格集合
        goods.setSkus(JsonUtils.toString(skus));// 所有sku的集合的json格式
        goods.setSpecs(specs);// 所有的可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());
        return goods;

    }

    private String chooseSegment(Object value, SpecParam p) {
        if (value == null) {
            return "其它";
        }
        double val = NumberUtils.toDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

}
