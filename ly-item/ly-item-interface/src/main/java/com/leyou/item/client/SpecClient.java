package com.leyou.item.client;


import com.leyou.item.pojo.SpecParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(serviceId = "item-service", path = "spec")
public interface SpecClient {
    /**
     * 调用接口，根据分类查允许被搜索的规格参数
     *
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("params")
    List<SpecParam> queryParamByGid(
            @RequestParam(name = "gid", required = false) Long gid,
            @RequestParam(name = "cid", required = false) Long cid,
            @RequestParam(name = "searching", required = false) Boolean searching
    );
}
