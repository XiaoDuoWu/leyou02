package com.leyou.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Map;
import java.util.Set;

@Data
@Document(indexName = "goods", type = "docs", shards = 1, replicas = 1)
public class Goods {
    @Id
    private Long id; // spuId
    private String subTitle;// 卖点                   用来展示的字段
    private String skus;// sku信息的json结构  集合转json，可以设置成keyword 不被分词

    private String all; // 所有需要被搜索的信息，包含标题，分类，甚至品牌。。
    private Long brandId;// 品牌id
    private Long cid3;// 3级分类id                     用来搜索的字段
    private Long createTime;// spu创建时间
    private Set<Long> price;// 价格 有多个，用set可以去重
    private Map<String, Object> specs;// 可搜索的规格参数，key是参数名，值是参数值
   /*
        不采用注解的方式来映射字段，因为像Map<String, Object> specs这种，如果取出来
        的是String类型的值，将来Elasticsearch会将String自动推断成2个字段，text 和 keyword
        所以后面会编写一个动态模版，用kibana动态映射

    */
}