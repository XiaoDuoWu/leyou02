package com.leyou.common.exceptions;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

@Getter
public class LyException extends RuntimeException {//继承RuntimeException，添加构造方法，包含状态码status
    private int status;

    public LyException( ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em, Throwable cause) {
        super(em.getMessage(), cause);
        this.status = em.getStatus();
    }
}
