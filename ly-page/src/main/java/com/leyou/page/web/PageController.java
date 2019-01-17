package com.leyou.page.web;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService goodsService;

    /**
     * 跳转到商品详情页
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping("item/{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id) {
        Map<String, Object> modelMap = goodsService.loalModel(id);
        // 放入模型
        model.addAllAttributes(modelMap);
//        如果nginx没有找到对应的html 则创建一个静态网页
        goodsService.syncCreateItemHtml(id);
        return "item";
    }
}
