package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient("item-service")
public interface GoodsClient {
    /**
     * 调用接口，查spu
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "rows", defaultValue = "5") Integer rows,
            @RequestParam(name = "key", required = false) String key,
            //是否上架
            @RequestParam(name = "saleable", required = false) Boolean saleable
    );

    /**
     * 调用接口，根据spu_id查spuDetail
     *
     * @param spu_id
     * @return
     */
    @GetMapping("spu/detail/{spu_id}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spu_id") Long spu_id);

    /**
     * 调用接口，根据spu_id查sku
     *
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long id);

    /**
     * 根据spuId查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuId(@PathVariable("id") Long id);
}
