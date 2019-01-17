package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.common.vo.PageResult;

import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.search.pojo.Goods;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IndexService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;

    @Autowired
    private ElasticsearchTemplate template;

    /**
     * 把一个spu转为一个Goods对象
     *
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu) {
        // 1 用来全文检索的字段, 包含标题、分类、品牌、...等
        // 1.1 查询分类
        String categoryNames = categoryClient.queryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                .stream().map(Category::getName).collect(Collectors.joining());
        // 1.2 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        String all = spu.getTitle() + categoryNames + brand.getName();

        // 2 spu下的所有sku的集合的json格式
        // 2.1 查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        // 2.2 取出需要的字段
        List<Map<String, Object>> skus = new ArrayList<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            map.put("title", sku.getTitle());
            skus.add(map);
        }
        // 3 spu下的所有sku的价格
        Set<Long> price = skuList.stream().map(Sku::getPrice).collect(Collectors.toSet());

        // 4 spu的可搜索的规格参数的键值对
        Map<String, Object> specs = new HashMap<>();

        // 4.1 查询规格参数key
        List<SpecParam> specParams = specClient.queryParamByGid(null, spu.getCid3(), true);

        // 4.2 查询规格参数值，在spuDetail中
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spu.getId());
        // 4.2.1 取出通用规格参数值
        Map<Long, Object> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, Object.class);
        // 4.2.2 取出特有规格参数值
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        // 4.3 填充map
        for (SpecParam specParam : specParams) {
            // 规格参数名称，作为key
            String key = specParam.getName();
            Object value = null;
            // 判断是否是通用属性
            if (specParam.getGeneric()) {
                // 通用属性
                value = genericSpec.get(specParam.getId());
            } else {
                // 特有属性
                value = specialSpec.get(specParam.getId());
            }
            // 处理value，判断是不是数值，如果是，需要分段处理
            if (specParam.getNumeric()) {
                value = chooseSegment(value, specParam);
            }

            specs.put(key, value);
        }

        Goods goods = new Goods();
        // 拷贝属性名一致的属性
        BeanUtils.copyProperties(spu, goods);
        // 其它属性，自己填写
        goods.setCreateTime(spu.getCreateTime().getTime());
        goods.setAll(all);// 用来全文检索的字段
        goods.setSkus(JsonUtils.toString(skus));// spu下的所有sku的集合的json格式
        goods.setPrice(price);// spu下的所有sku的价格
        goods.setSpecs(specs);// spu的可搜索的规格参数的键值对
        return goods;
    }

    private String chooseSegment(Object value, SpecParam p) {
        if (value == null || value.toString().equals("")) {
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
