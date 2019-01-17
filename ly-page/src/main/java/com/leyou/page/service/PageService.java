package com.leyou.page.service;

import com.leyou.item.client.BrandClient;
import com.leyou.item.client.CategoryClient;
import com.leyou.item.client.GoodsClient;
import com.leyou.item.client.SpecClient;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.Spu;
import com.sun.org.apache.bcel.internal.generic.NEW;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Service
@Slf4j
public class PageService {
    //    创建线程池
    private static final ExecutorService es = Executors.newFixedThreadPool(20);
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecClient specClient;
    @Value("${ly.page.destPath}")
    public String desctPath;
    @Autowired
    private SpringTemplateEngine templateEngine;
    public Map<String, Object> loalModel(Long spuId) {
        // 查询spu
        Spu spu = goodsClient.querySpuId(spuId);
        // 查询分类
        List<Category> categories = categoryClient.queryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询规格参数
        List<SpecGroup> specs = specClient.querySpecs(spu.getCid3());
//         封装数据
        Map<String, Object> map = new HashMap<>();
        map.put("specs", specs);
        map.put("brand", brand);
        map.put("categories", categories);
        map.put("skus", spu.getSkus());
        map.put("detail", spu.getSpuDetail());

        // 防止重复数据
        spu.setSkus(null);
        spu.setSpuDetail(null);
        map.put("spu", spu);
        return map;
    }

    //    利用线程池 完成静态网页的写入
    public void syncCreateItemHtml(Long id) {
      es.submit(() -> createHtml(id));
    }
//创建静态网页
    public void createHtml(Long id) {
//        准备上下文的对象
        Context context = new Context();
        context.setVariables(loalModel(id));
//        文件输出路径
        File dir = new File(desctPath);
//        判断文件路径是否存在 如果不存在 就创建
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, id + ".html");
        // 准备输出流
        try(PrintWriter writer = new PrintWriter(file, "UTF-8")){
//           将模板渲染并输出到目的地
            templateEngine.process("item", context, writer);
        }catch (IOException e){
            log.error("【静态页服务】创建商品静态页失败,商品id：{}", id, e);
        }
    }




}
