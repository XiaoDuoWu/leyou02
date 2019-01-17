package com.leyou.item.client;

import com.leyou.item.pojo.Brand;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(serviceId = "item-service",path = "brand")
public interface BrandClient {
    /**
     * 调用接口，查商品的品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    Brand queryBrandById(@PathVariable("id") Long id);


    @GetMapping("list")
    List<Brand> queryByIdList(@RequestParam("ids") List<Long> ids);
}
