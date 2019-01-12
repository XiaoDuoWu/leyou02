package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 通过继承ElasticsearchRepository来对索引库进行crud操作
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
