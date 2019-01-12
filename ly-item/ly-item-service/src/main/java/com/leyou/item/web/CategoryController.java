package com.leyou.item.web;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父类目id查询子类目的集合
     *
     * @param pid 父id，为0 的是顶级父类的id
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryByPid(@RequestParam("pid") Long pid) {
        List<Category> list = categoryService.queryByPid(pid);
        return ResponseEntity.status(200).body(list);
    }

    /**
     * 根据id查询分类，因为有3级分类的id，要把3级分类都查出来
     *
     * @param ids
     * @return
     */
    @GetMapping("list/ids")
    public ResponseEntity<List<Category>> queryListByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(categoryService.queryByIds(ids));
    }

}