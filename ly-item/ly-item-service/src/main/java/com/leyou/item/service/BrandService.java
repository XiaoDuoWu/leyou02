package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryByPage(String key, String sortBy, Integer page, Integer rows, Boolean desc) {
        //分页
        PageHelper.startPage(page, rows);
        //搜索过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            //有搜素关键字
            example.createCriteria().orLike("name", "%" + key + "%").orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByCaluse = sortBy + (desc ? " Desc" : " Asc");//空格别忘记加上
            example.setOrderByClause(orderByCaluse);
        }
        //查询结果
        List<Brand> brands = brandMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //封装并返回
        PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult<>(info.getTotal(), brands);
    }

    public void saveBrand(Brand brand, List<Long> cidList) {
        // 品牌的新增
        int count = brandMapper.insert(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
        }
        // 新增中间表
        for (Long cid : cidList) {
            count = brandMapper.saveCategoryBrand(cid, brand.getId());
            if (count != 1) {
                throw new LyException(ExceptionEnum.BRAND_EDIT_ERROR);
            }
        }
    }

    public Brand queryBrandById(long id) {
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    /**
     * 通过分类id查品牌名称，无法直接查，有tb_brand 和 tb_category_brand表
     * 通过内联查询
     *
     * @param cid
     * @return
     */
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
    public List<Brand> queryBrandByIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}
