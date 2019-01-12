package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.pojo.Category;


/**
 * IdListMapper:通用Mapper接口,根据idList操作
 * InsertMapper:保存实体类
 */

//抽取成通用 的Mapper接口
//public interface CategoryMapper extends Mapper<Category> , IdListMapper<Category,Long > , InsertMapper<Category> {
public interface CategoryMapper extends BaseMapper<Category> {
}
