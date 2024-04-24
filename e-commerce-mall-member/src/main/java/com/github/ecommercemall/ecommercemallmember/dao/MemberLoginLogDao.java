package com.github.ecommercemall.ecommercemallmember.dao;

import com.github.ecommercemall.ecommercemallmember.entity.MemberLoginLogEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员登录记录
 * 
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:02:02
 */
@Mapper
public interface MemberLoginLogDao extends BaseMapper<MemberLoginLogEntity> {
	
}
