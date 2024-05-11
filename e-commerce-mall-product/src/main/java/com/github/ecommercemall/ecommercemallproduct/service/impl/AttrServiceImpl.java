package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.fasterxml.jackson.databind.util.BeanUtil;
import com.github.ecommercemall.ecommercemallproduct.dao.AttrAttrgroupRelationDao;
import com.github.ecommercemall.ecommercemallproduct.entity.AttrAttrgroupRelationEntity;
import com.github.ecommercemall.ecommercemallproduct.vo.AttrVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.AttrDao;
import com.github.ecommercemall.ecommercemallproduct.entity.AttrEntity;
import com.github.ecommercemall.ecommercemallproduct.service.AttrService;
import org.springframework.transaction.annotation.Transactional;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    // 注意只要有两个操作，而且是连续的，那么必须使用Transactional注解维护事务一致性
    @Transactional
    @Override
    public void saveAttr(AttrVO attr) {
        final AttrEntity attrEntity = new AttrEntity();
        // 注意这里必须两个类的属性名字是一一对应的才能直接映射过去
        BeanUtils.copyProperties(attr, attrEntity);
        // 保存基本数据
        this.save(attrEntity);
        // 保存关联关系
        final AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
        // 注意两个东西不一样attr，attrEntity
        attrAttrgroupRelationEntity.setAttrId(attrEntity.getAttrId());
        attrAttrgroupRelationEntity.setAttrGroupId(attr.getAttrGroupId());
        attrAttrgroupRelationDao.insert(attrAttrgroupRelationEntity);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }

}