package com.leyou.service;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecClient;
import com.leyou.item.pojo.*;
import com.leyou.pojo.SearchResult;
import com.leyou.search.pojo.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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
public class SearchService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecClient specClient;
    @Autowired
    private ElasticsearchTemplate template;


    public PageResult<Goods> search(SearchRequest request) {
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        // 原生查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 0 通过source过滤，控制返回的字段  返回给前端必须的字段
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        // 1 分页  页数是0基的 减一
        int page = request.getPage() - 1;
        int size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page, size));
        // 2 关键字搜索
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        String sortBy = request.getSortBy();
        Boolean desc = request.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }
        // 3 聚合
        // 3.1 对分类聚合
        String categoryAggName = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        // 3.2 对品牌聚合
        String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 4 查询结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        // 5 解析结果
        // 5.1 分页结果
        int totalPages = result.getTotalPages();
        long total = result.getTotalElements();
        List<Goods> list = result.getContent();
        // 5.2 处理过滤条件
        List<Map<String, Object>> filterList = new ArrayList<>();
        // 5.3 解析聚合结果
        // 5.3.1 解析分类聚合
        Aggregations aggs = result.getAggregations();

        List<Long> idList = handleCategoryAgg(aggs.get(categoryAggName), filterList);
        // 5.3.2 解析品牌聚合
        handleBrandAgg(aggs.get(brandAggName), filterList);

        // 6 对规格参数进行聚合
        // 6.1 判断分类的数量是否为1
        if (idList != null && idList.size() == 1) {
            // 6.2 处理规格参数
            handleSpec(idList.get(0), filterList, basicQuery);
        }
        // 7 封装并返回
        return new SearchResult(total, totalPages, list, filterList);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        // 1.构建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 2.搜索条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 3.过滤条件
        Map<String, String> filters = request.getFilters();

        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // 处理规格参数的key
            if (!"cid3".equals(key) && !"brandId".equals(key)) {
                key = "specs." + key;
            }
            queryBuilder.filter(QueryBuilders.termQuery(key, value));
        }
        return queryBuilder;
    }

    private void handleSpec(Long cid, List<Map<String, Object>> filterList, QueryBuilder basicQuery) {
        // 1.查询需要聚合的规格参数
        List<SpecParam> specParams = specClient.queryParamByGid(null, cid, true);

        // 2.对规格聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2.1 添加过滤条件
        queryBuilder.withQuery(basicQuery);
        // 2.2 减少分页size，以减少搜索结果
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 2.3 聚合
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }

        // 3.得到聚合结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);

        // 4.解析结果，封装到filterList中
        Aggregations aggs = result.getAggregations();
        for (SpecParam specParam : specParams) {
            String name = specParam.getName();
            // 取出terms
            StringTerms terms = aggs.get(name);
            List<String> options = terms.getBuckets().stream()
                    .map(bucket -> bucket.getKeyAsString()).filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            // 准备过滤项
            Map<String, Object> map = new HashMap<>();
            map.put("k", name);
            map.put("options", options);
            filterList.add(map);
        }
    }

    private void handleBrandAgg(LongTerms terms, List<Map<String, Object>> filterList) {
        // 解析terms，取出品牌的id
        List<Long> idList = terms.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        // 根据id查询品牌对象
        List<Brand> brands = brandClient.queryByIdList(idList);
        // 准备过滤项
        Map<String, Object> map = new HashMap<>();
        map.put("k", "brandId");
        map.put("options", brands);
        filterList.add(map);
    }

    private List<Long> handleCategoryAgg(LongTerms terms, List<Map<String, Object>> filterList) {
        // 解析terms，取出品牌的id
        List<Long> idList = terms.getBuckets().stream()
                .map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        // 根据id查询品牌对象
        List<Category> categories = categoryClient.queryListByIds(idList);
        // 准备过滤项
        Map<String, Object> map = new HashMap<>();
        map.put("k", "cid3");
        map.put("options", categories);
        filterList.add(map);
        return idList;
    }
}