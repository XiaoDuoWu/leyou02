package com.leyou.common.vo;

import com.leyou.common.exceptions.LyException;
import lombok.Data;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 * vo:view Object 视图对象 专门返回给页面的视图
 */
@Data
public class ExceptionResult {
    private String message;
    private int status;
    private String timeStamp;

    public ExceptionResult(LyException e) {
        this.status = e.getStatus();
        this.message = e.getMessage();
        this.timeStamp = DateTime.now().toString("yyyy-MM-dd"); //导入joda-time依赖，用这个来获取时间戳
    }
}
