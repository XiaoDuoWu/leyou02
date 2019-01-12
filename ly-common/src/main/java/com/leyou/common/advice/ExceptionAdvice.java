package com.leyou.common.advice;

import com.leyou.common.exceptions.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice//会拦截所有加了`@Controller`的类 需要添加spring-web的依赖
public class ExceptionAdvice {
    //@ExceptionHandler(RuntimeException.class)//作用在方法上，声明要处理的异常类型
    @ExceptionHandler(LyException.class)//作用在方法上，声明要处理的异常类型
    //public ResponseEntity<String> handleException(RuntimeException e){
    public ResponseEntity<ExceptionResult> handleException(LyException e) {
        //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());//此时报的异常写死了，异常让Controller传递过来
        //return ResponseEntity.status(e.getStatus()).body(e.getMessage() );//通过e.getStatus获取响应码
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));//通过new ExceptionResult()对象
    }
}
