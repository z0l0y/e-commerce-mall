package com.github.ecommercemall.ecommercemallproduct.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryService;
import com.github.common.utils.R;


/**
 * 商品三级分类
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
@RestController
@RequestMapping("ecommercemallproduct/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 列表，查出所有分类以及子分类，以树形结构组装起来
     */
    @RequestMapping("/list/tree")
    // @RequiresPermissions("ecommercemallproduct:category:list")
    public R list() {
        List<CategoryEntity> list = categoryService.listWithTree();
        return R.ok().put("data", list);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    // @RequiresPermissions("ecommercemallproduct:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ecommercemallproduct:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ecommercemallproduct:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ecommercemallproduct:category:delete")
    public R delete(@RequestBody Long[] catIds) {
        categoryService.removeByIds(Arrays.asList(catIds));
        return R.ok();
    }
}
