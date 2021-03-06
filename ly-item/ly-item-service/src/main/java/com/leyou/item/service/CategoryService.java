package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public Category queryCategoryByid(Long id) {
        Category category = categoryMapper.selectByPrimaryKey(id);
        return category;
    }


    public List<Category> queryByIds(List<Long> ids) {
        /**
         * categoryMapper接口继承了IdListMapper接口，可以根据idList查
         */
        List<Category> categories = categoryMapper.selectByIdList(ids);
        //对集合做非空判断
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    public List<Category> queryBreadById(Long id) {
        List<Category> categoryList = new ArrayList<>();
        Category category3 = categoryMapper.selectByPrimaryKey(id);
        Long parentId2 = category3.getParentId();
        Category category2 = categoryMapper.selectByPrimaryKey(parentId2);
        Long parentId1 = category2.getParentId();
        Category category1 = categoryMapper.selectByPrimaryKey(parentId1);
        categoryList.add(category1);
        categoryList.add(category2);
        categoryList.add(category3);
        return categoryList;
    }
}
