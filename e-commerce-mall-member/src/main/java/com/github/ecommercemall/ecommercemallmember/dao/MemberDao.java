package com.github.ecommercemall.ecommercemallmember.dao;

import com.github.ecommercemall.ecommercemallmember.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:02:03
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
