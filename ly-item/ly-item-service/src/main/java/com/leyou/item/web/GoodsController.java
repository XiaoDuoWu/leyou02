package com.leyou.item.web;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 查spu，带分页
     *
     * @param page
     * @param rows
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<Spu>> querySpuByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "rows", defaultValue = "5") Integer rows,
            @RequestParam(name = "key", required = false) String key,
            //是否上架
            @RequestParam(name = "saleable", required = false) Boolean saleable
    ) {
        return ResponseEntity.ok(goodsService.querySpuByPage(page, rows, key, saleable));
    }

    /**
     * 新增商品
     *
     * @param spu
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(201).build();
    }

    /**
     * 根据id 和是true 还是false来上下架商品
     *
     * @param id
     * @param saleable
     * @return
     */
    @PutMapping("goods/{id}/{saleable}")
    public ResponseEntity<Void> updateSaleable(@PathVariable("id") Long id, @PathVariable("saleable") Boolean saleable) {
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.status(204).build();
    }

    /**
     * 根据spu_id来查spudetail表  sku-skuDetail  1对1关系
     *
     * @param spu_id
     * @return
     */
    @GetMapping("spu/detail/{spu_id}")
    public ResponseEntity<SpuDetail> queryBySpuId(@PathVariable("spu_id") Long spu_id) {
        return ResponseEntity.ok(goodsService.queryBySpuId(spu_id));
    }


    /**
     * 根据spu_id查sku  1对多的关系
     *
     * @param id
     * @return
     */
    @GetMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.querySkuBySpuId(id));
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    //根据spuId查询spu
    @GetMapping("spu/{id}")
    public ResponseEntity<Spu> querySpuById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(goodsService.querySpuById(id));
    }

    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(goodsService.querySkuByIds(ids));
    }
//减库存 方法
@PostMapping("stock/decrease")
public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> cartDTOS){
    goodsService.decreaseStock(cartDTOS);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}

}
