package com.github.ecommercemall.ecommercemallproduct.controller;

import java.util.Arrays;
import java.util.Map;

import com.github.common.valid.AddGroup;
import com.github.common.valid.UpdateGroup;
import com.github.common.valid.UpdateStatusGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.ecommercemall.ecommercemallproduct.entity.BrandEntity;
import com.github.ecommercemall.ecommercemallproduct.service.BrandService;
import com.github.common.utils.PageUtils;
import com.github.common.utils.R;


/**
 * 品牌
 *
 * @author zoloy
 * @email zoloy@gmail.com
 * @date 2024-04-23 12:30:49
 */
@RestController
@RequestMapping("ecommercemallproduct/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("ecommercemallproduct:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    // @RequiresPermissions("ecommercemallproduct:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("ecommercemallproduct:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {
/*        if (bindingResult.hasErrors()) {
            Map<String, String> map = new HashMap<>();
            // 1.获取校验的错误结果
            bindingResult.getFieldErrors().forEach(item -> {
                // 得到错误提示
                String message = item.getDefaultMessage();
                // 获取到错误属性的名字
                String field = item.getField();
                map.put(field, message);
            });
            return R.error(400, "提交的数据不合法").put("data", map);
        } else {

        }*/
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("ecommercemallproduct:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }

    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    // @RequiresPermissions("ecommercemallproduct:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("ecommercemallproduct:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
