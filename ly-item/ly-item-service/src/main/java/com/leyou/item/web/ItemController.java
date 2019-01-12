package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.pojo.Item;
import com.leyou.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ItemController {
    @Autowired
    private ItemService itemService;
    @PostMapping("item")
    public ResponseEntity<Item> saveItem(Item item){
        if (item.getPrice()==null){
            //throw new LyException("价格不能为空");//思考：异常信息自定义，不规范，采用枚举
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        Item saveItem = itemService.saveItem(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saveItem);
    }
}
