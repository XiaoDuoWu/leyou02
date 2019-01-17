package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends BaseMapper<Brand> {
    @Insert("insert into tb_category_brand values(#{cid},#{bid})")
    int saveCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);

    @Select("select b.id,b.name,b.image from tb_brand b INNER JOIN tb_category_brand cb on b.id = cb.brand_id where cb.category_id=#{cid}")
    List<Brand> queryBrandByCid(Long cid);
}