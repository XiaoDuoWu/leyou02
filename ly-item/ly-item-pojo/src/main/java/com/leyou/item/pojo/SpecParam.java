package com.leyou.item.pojo;


import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_spec_param")
public class SpecParam {
    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long cid;
    private Long groupId;
    private String name;
    @Column(name = "`numeric`")//`numeric`加两个点的目的是：把关键字转为普通字符串,也可以在 yml配置文件中统一配置
    //numberic：是否为数值类型
    private Boolean numeric;
    //'数字类型参数的单位
    private String unit;
    //是否是sku通用属性
    private Boolean generic;
    //是否用于搜索过滤
    private Boolean searching;
    //添加分段间隔值
    private String segments;
}
