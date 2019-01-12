package com.leyou.item.client;

import com.leyou.item.pojo.Category;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(serviceId = "item-service",path = "category")
public interface CategoryClient {
    /**
     * 调用接口，查商品3级所有的分类
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    List<Category> queryListByIds(@RequestParam("ids") List<Long> ids) ;
}
