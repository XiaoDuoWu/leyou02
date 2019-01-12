package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;



/**
 * IdListMapper:通用Mapper接口,根据idList操作
 * InsertMapper:保存实体类
 */

//自动注册 Mapper 接口标记
@RegisterMapper
public interface BaseMapper<T> extends Mapper<T>, IdListMapper<T,Long >, InsertListMapper<T> {
}
