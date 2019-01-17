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
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

public class SearchServiceDemo {
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;

    /**
     * 把一个spu转成一个goods
     * 需求：
     * 查询spu（subtitle、cid、bid）
     * 根据spu查询sku（查image、price、title、id）
     * 根据spu查询spuDeatail（specs的值）
     * 查询当前分类下需要用来搜索过滤的规格参数（specs的key）
     * 根据id查分类（cname）
     * 根据id查品牌（bname）
     *
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) {
        Goods goods = new Goods();
        // 1.1 分类名称，搜集成字符串，只做查询用
        String cnames = categoryClient.queryListByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                .stream()
                .map(Category::getName)
                .collect(Collectors.joining(","));
        // 1.2 品牌名称 通过品牌id查品牌
        String bname = brandClient.queryBrandById(spu.getBrandId()).getName();
        // 1.3 查询标题 (需要分类名称，品牌名称，标题。。)
        String all = spu.getTitle() + cnames + bname;
        // 2 spu下的所有sku的JSON数组 通过spuid查所有sku 只需要（查sku的image、price、title、id）
        /**
         * 如何取出sku中想要的属性和对应的值，把对象用map来接收，都是键值对的形式
         */
        List<Sku> skus = goodsClient.querySkuBySpuId(spu.getId());
        List<Map<String, Object>> skuList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        for (Sku sku : skus) {
            map.put("id", sku.getId());
            //有多张图片，取一张即可，
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("price", sku.getPrice());
            map.put("title", sku.getTitle());
            skuList.add(map);
        }
        // 3 当前spu下所有sku的价格的集合 用set接收，可去重
        Set<Long> price = skus.stream().map(Sku::getPrice).collect(Collectors.toSet());
        //4.查询spu下所有可搜索的规格参数的键值对 用map接受
        HashMap<String, Object> specMap = new HashMap<>();
        //4.1查询map的key，即规格参数表中的 name值
        List<SpecParam> specParams = specClient.queryParamByGid(null, spu.getCid3(), true);
        //4.2查询map的value，即spu详情表中的 规格的值
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        //通过看数据库中规格参数的值，通用规格是一个key对应一个value，特有规格是一个key对应一个集合
        //4.2.1查通用规格参数的值 一个key对应一个value，用map结构
        Map<Long, Object> genericMap = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, Object.class);
        //4.2.2查特有规格参数的值 一个key对应一个集合 复杂结构
        Map<Long, List<String>> specialMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });
        //4.3 将规格参数表中的 name值和spu详情表中的 规格的值对应起来
        for (SpecParam specParam : specParams) {
            //4.3.1判断是通用规格参数还是特有规格参数
            if (specParam.getGeneric()){
                //通用规格参数
                specMap.put(specParam.getName(),genericMap.get(specParam.getId()));
            }else {
                //特有规格参数★★★★★★★★★此处继续----------
                specMap.put(specParam.getName(),specialMap.get(specParam.getId()));
            }

        }

        //拷贝属性名一致的属性（bid、c3id、id、createTime、subtitle）
        BeanUtils.copyProperties(spu, goods);
        //不一致的属性手动设置
        goods.setAll(all); // 用来全文检索的字段
        goods.setSkus(JsonUtils.toString(skuList)); // spu下所有sku的json格式
        goods.setPrice(price); //spu下所有sku的价格
        goods.setSpecs(null); //TODO spu下所有可搜索的规格参数的键值对
        return goods;
    }

    private String chooseSegment(Object value, SpecParam p) {
        if(value == null || StringUtils.isBlank(value.toString())){
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
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
