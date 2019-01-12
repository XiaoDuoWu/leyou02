package com.leyou.common.enums;

import lombok.Getter;

@Getter
public enum ExceptionEnum {
    PRICE_CANNOT_BE_NULL(400,"价格不能为空") , //有多个用逗号隔开，分号结束
    CATEGORY_NOT_FOUND(404,"商品分类不存在") , //有多个用逗号隔开，分号结束
    BRAND_NOT_FOUND(404, "品牌不存在"),
    BRAND_EDIT_ERROR(400, "品牌参数有误"),
    FILE_UPLOAD_ERROR(500, "文件上传异常"),
    INVALID_FILE_TYPE(400, "无效的文件类型"),
    SPEC_GROUP_NOT_FOUND(404, "规格组不存在"),
    SPEC_PARAM_NOT_FOUND(404, "规格参数不存在"),
    GOODS_NOT_FOUND(404, "商品不存在"),
    SPEC_GROUP_NAME_CANNOT_BE_NULL(400, "规格组名不能为空"),
    SPEC_GROUP_INSERT_ERROR(400, "规格组新增失败"),
    SPEC_GROUP_NAME_EXIST(400, "规格组名已存在"),
    SPEC_GROUP_EDIT_ERROR(400, "规格组名编辑失败"),
    SERVER_ERROR(500, "服务器异常"),
    SPEC_PARAM_NAME_CANNOT_BE_NULL(400, "规格参数名不能为空"),
    SPEC_PARAM_NAME_EXIST(400, "规格参数名已存在"),
    SPEC_PARAM_EDIT_ERROR(400, "规格组名编辑失败"),
    GOODS_INSERT_ERROR(400, "商品新增失败"),
    GOODS_EDIT_ERROR(400, "商品编辑失败"),
    ;
    //定义成员变量
    private String message;
    private int status;
    //构造函数

    ExceptionEnum(int status, String message ) {
        this.message = message;
        this.status = status;
    }
}
