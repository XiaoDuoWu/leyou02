package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.service.SpecService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("spec")
public class SpecController {

    @Autowired
    private SpecService specService;
    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> queryGroupByCid(@PathVariable("cid") Long cid){
        return ResponseEntity.ok(specService.queryGroupByCid(cid));
    }

    /**
     * 查规格参数 （根据gid、cid、searching组合条件）
     * 改造：在原来的根据 gid（规格组id)查询规格参数的接口上，添加一个参数：cid，即商品分类id，
     * 添加是否搜索、是否为通用属性等条件
     * @param gid
     * @return
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>>queryParamByGid(
            @RequestParam(name = "gid",required = false)Long gid,
            @RequestParam(name = "cid",required = false)Long cid,
            @RequestParam(name = "searching",required = false)Boolean searching
    ){
        return ResponseEntity.ok(specService.queryParamByGid(gid,cid,searching));
    }
    @PostMapping("group")
    public ResponseEntity<Void> saveGroup(@RequestBody SpecGroup specGroup){
        if (StringUtils.isBlank(specGroup.getName())){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NAME_CANNOT_BE_NULL);
        }
        specService.saveGroup(specGroup);
        return ResponseEntity.status(201).build();
    }
    @PutMapping("group")
    public ResponseEntity<Void> updateGroup(@RequestBody SpecGroup specGroup){
        if (StringUtils.isBlank(specGroup.getName())){
            throw new LyException(ExceptionEnum.SPEC_GROUP_NAME_CANNOT_BE_NULL);
        }
         specService.updateGroup(specGroup);
        return ResponseEntity.status(204).build();
    }
    @DeleteMapping("group/{gid}")
    public ResponseEntity<Void> deleteGroupById(@PathVariable("gid")Long gid){
        specService.deleteGroupById(gid);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("param")
    public ResponseEntity<Void> saveParam(@RequestBody SpecParam specParam){
        if (StringUtils.isBlank(specParam.getName())){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NAME_CANNOT_BE_NULL);
        }
        specService.saveParam(specParam);
        return ResponseEntity.status(201).build();
    }
    @PutMapping("param")
    public ResponseEntity<Void> updateParam(@RequestBody SpecParam specParam){
        if (StringUtils.isBlank(specParam.getName())){
            throw new LyException(ExceptionEnum.SPEC_PARAM_NAME_CANNOT_BE_NULL);
        }
        specService.updateParam(specParam);
        return ResponseEntity.status(204).build();
    }
}
