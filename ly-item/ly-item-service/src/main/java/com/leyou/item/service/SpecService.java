package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecService {
    @Autowired
    private SpecGroupMapper specGroupMapper;
    @Autowired
    private SpecParamMapper specParamMapper;

    public List<SpecGroup> queryGroupByCid(Long cid) {
        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(cid);
        List<SpecGroup> list = specGroupMapper.select(specGroup);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_NOT_FOUND);
        }
        return list;
    }

    public List<SpecParam> queryParamByGid(Long gid, Long cid, Boolean searching) {
        SpecParam specParam = new SpecParam();
        specParam.setGroupId(gid);
        specParam.setCid(cid);
        specParam.setSearching(searching);
        List<SpecParam> list = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        return list;
    }

    @Transactional
    public void saveGroup(SpecGroup specGroup) {
        SpecGroup s = new SpecGroup();
        List<SpecGroup> list = specGroupMapper.select(s);
        for (SpecGroup group : list) {
            if (group.getName().equals(specGroup.getName())) {
                throw new LyException(ExceptionEnum.SPEC_GROUP_NAME_EXIST);
            }
        }
        int i = specGroupMapper.insert(specGroup);
        if (i != 1) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_INSERT_ERROR);
        }
    }

    @Transactional
    public void updateGroup(SpecGroup specGroup) {
        SpecGroup s = new SpecGroup();
        List<SpecGroup> list = specGroupMapper.select(s);
        for (SpecGroup group : list) {
            if (group.getName().equals(specGroup.getName())) {
                throw new LyException(ExceptionEnum.SPEC_GROUP_NAME_EXIST);
            }
        }
        s.setId(specGroup.getId());
        s.setCid(specGroup.getCid());
        s.setName(specGroup.getName());
        int i = specGroupMapper.updateByPrimaryKey(s);
        if (i != 1) {
            throw new LyException(ExceptionEnum.SPEC_GROUP_EDIT_ERROR);
        }
    }

    @Transactional
    public void deleteGroupById(Long gid) {
        int i = specGroupMapper.deleteByPrimaryKey(gid);
        SpecParam param = new SpecParam();
        param.setGroupId(gid);
        specParamMapper.delete(param);
        if (i != 1) {
            throw new LyException(ExceptionEnum.SERVER_ERROR);
        }
    }

    @Transactional
    public void saveParam(SpecParam specParam) {
        SpecParam s = new SpecParam();
        List<SpecParam> list = specParamMapper.select(s);
        for (SpecParam param : list) {
            if (param.getName().equals(specParam.getName())) {
                throw new LyException(ExceptionEnum.SPEC_PARAM_NAME_EXIST);
            }
        }
        specParamMapper.insert(specParam);
    }

    @Transactional
    public void updateParam(SpecParam specParam) {
        SpecParam s = new SpecParam();
        s.setCid(specParam.getCid());
        s.setGroupId(specParam.getGroupId());
        s.setName(specParam.getName());
        s.setNumeric(specParam.getNumeric());
        s.setUnit(specParam.getUnit());
        s.setGeneric(specParam.getGeneric());
        s.setSearching(specParam.getSearching());
        s.setSegments(specParam.getSegments());
        s.setId(specParam.getId());
        int i = specParamMapper.updateByPrimaryKey(s);
        if (i != 1) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_EDIT_ERROR);
        }
    }

    //
    public List<SpecGroup> querySpecs(Long cid) {
//      查询出 规格组
        List<SpecGroup> specGroups = queryGroupByCid(cid);

        List<SpecParam> specParams = queryParamByGid(null, cid, null);
//        把specParams转换成Map
        Map<Long, List<SpecParam>> map = specParams.stream().collect(Collectors.groupingBy(SpecParam::getGroupId));
        for (SpecGroup specGroup : specGroups) {
            specGroup.setParams(map.get(specGroup.getId()));
        }
        return specGroups;
    }
}
