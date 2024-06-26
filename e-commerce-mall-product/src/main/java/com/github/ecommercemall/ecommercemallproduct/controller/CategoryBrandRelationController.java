package com.github.ecommercemall.ecommercemallproduct.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.ecommercemall.ecommercemallproduct.entity.CategoryBrandRelationEntity;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryBrandRelationService;
import com.github.common.utils.PageUtils;
import com.github.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
@RestController
@RequestMapping("ecommercemallproduct/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取当前品牌关联的所有列表分类
     */
    @GetMapping("/catelog/list")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:list")
    public R list(@RequestParam("brandId") Long brandId) {
        List<CategoryBrandRelationEntity> list =
                categoryBrandRelationService.list(new QueryWrapper<CategoryBrandRelationEntity>()
                        .eq("brand_id", brandId));
        return R.ok().put("data", list);

    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {

        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ecommercemallproduct:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
