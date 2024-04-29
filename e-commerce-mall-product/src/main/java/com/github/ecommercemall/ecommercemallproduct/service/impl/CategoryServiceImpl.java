package com.github.ecommercemall.ecommercemallproduct.service.impl;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.CategoryDao;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryService;

import javax.xml.ws.soap.Addressing;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有pms分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 组装成父子的树形结构
        // 简单的说，下面的操作是将树的每一个节点都传递进去，然后递归得到它的全部后代，最后返回最上层的分类就行了
        // 因为下面的节点我们都排好了，然后上面的也要排的意思是我们下面的那个函数仅仅只是排了n-1层
        // 首先找到一级分类
        List<CategoryEntity> level1Menus =
                // 流中的每一个item是CategoryEntity类型实体类
                categoryEntities.stream()
                        // 先过滤all，获取到parent_cid为0的CategoryEntit类型实体类，再形成一个新的流
                        .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                        // 该操作是将一个的菜单，和全部的分类菜单传递给getChildren函数
                        .map(menu -> {
                            menu.setChildren(getChildren(menu, categoryEntities));
                            return menu;
                        })
                        // 排序字段负责显示分类的结果谁先展示谁后展示（简单的说就是显示也有优先级，这个决定了我们前端显示的顺序）
                        .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                        // 搜集获得到的流对象组装成一个list
                        .collect(Collectors.toList());
        return level1Menus;
    }

    // 递归查找所有菜单的子菜单
    // 这边理解的话我们可以用树的角度来理解
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream()
                // 过滤，将all中所有的实体类的ID和我们传递的item的ID进行比对，留下ID相等的实体类
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                // 找到子菜单
                // 可能会有多个层级，我们这里要采用递归，一直得到树最底层的children
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                // 对children依次排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                // 将最后获取到的流封装成一个list
                .collect(Collectors.toList());
        return children;
    }

}