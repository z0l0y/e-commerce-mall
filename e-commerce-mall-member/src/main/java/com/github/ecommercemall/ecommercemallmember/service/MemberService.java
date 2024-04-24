package com.github.ecommercemall.ecommercemallmember.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.common.utils.PageUtils;
import com.github.ecommercemall.ecommercemallmember.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 19:02:03
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

