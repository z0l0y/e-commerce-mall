package com.github.ecommercemall.ecommercemallproduct.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryBrandRelationService;
import com.github.ecommercemall.ecommercemallproduct.vo.Catelog2VO;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallproduct.dao.CategoryDao;
import com.github.ecommercemall.ecommercemallproduct.entity.CategoryEntity;
import com.github.ecommercemall.ecommercemallproduct.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.xml.ws.soap.Addressing;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //  查出所有pms分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        //  组装成父子的树形结构
        //  简单的说，下面的操作是将树的每一个节点都传递进去，然后递归得到它的全部后代，最后返回最上层的分类就行了
        //  因为下面的节点我们都排好了，然后上面的也要排的意思是我们下面的那个函数仅仅只是排了n-1层
        //  首先找到一级分类
        List<CategoryEntity> level1Menus =
                //  流中的每一个item是CategoryEntity类型实体类
                categoryEntities.stream()
                        //  先过滤all，获取到parent_cid为0的CategoryEntit类型实体类，再形成一个新的流
                        .filter(categoryEntity -> categoryEntity.getParentCid() == 0)
                        //  该操作是将一个的菜单，和全部的分类菜单传递给getChildren函数
                        .map(menu -> {
                            menu.setChildren(getChildren(menu, categoryEntities));
                            return menu;
                        })
                        //  排序字段负责显示分类的结果谁先展示谁后展示（简单的说就是显示也有优先级，这个决定了我们前端显示的顺序）
                        .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                        //  搜集获得到的流对象组装成一个list
                        .collect(Collectors.toList());
        return level1Menus;
    }

    //  [2,255,21]
    //  有一说一,如果找不到BUG是怎么来的,建议用断点一个一个看
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @CacheEvict:失效模式，直接清空指定分区的缓存
     * @CachePut:双写模式，需要有返回值，也就是说返回值为void的不能缓存 1、同时进行多种缓存操作：@Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category",allEntries = true)
     * 3、存储同一类型的数据，都可以指定为同一分区，分区名默认就是缓存的前缀
     */
    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categorys'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })
    // 只要调用了updateCascade方法修改了数据，那么就会清空指定key的缓存
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());

    }

    /**
     * 每一个需要缓存的数据我们都来指定要放到那个名字的缓存。【缓存的分区(按照业务类型分)】
     * 代表当前方法的结果需要缓存，如果缓存中有，方法都不用调用，如果缓存中没有，会调用方法。最后将方法的结果放入缓存
     * 默认行为:
     * 如果缓存中有，方法不再调用
     * key是默认生成的: 缓存的名字::SimpleKey [](自动生成key值) 如：category::SimpleKey []
     * 缓存的value值，默认使用jdk序列化机制，将序列化的数据存到redis中
     * 默认时间是 -1：表示永不过期
     * <p>
     * 自定义操作：<a href="https://docs.spring.io/spring-framework/reference/integration/cache/annotations.html">...</a>
     * 指定生成缓存的key：key属性指定，接收一个SpEL(Spring Exception Language),注意只是单纯的字符串的话要记得加上单引号，否则会被视为变量处理
     * 指定缓存的数据的存活时间:配置文档中修改存活时间,记得要在application.properties配置文件中进行配置
     * 数据保存的格式：将数据保存为json格式
     * <p>
     * <p>
     * 这下面的都是缓存使用的很好总结：
     * 4、Spring-Cache的不足之处：
     * 1）、读模式
     * 缓存穿透：查询一个null数据。解决方案：缓存空数据
     * 缓存击穿：大量并发进来同时查询一个正好过期的数据。解决方案：加锁 ? 默认是无加锁的;使用sync = true来解决击穿问题
     * 缓存雪崩：大量的key同时过期。解决：加随机时间。加上过期时间
     * 2)、写模式：（缓存与数据库一致）
     * 1）、读写加锁。
     * 2）、引入Canal,感知到MySQL的更新去更新Redis
     * 3）、读多写多，直接去数据库查询就行
     * <p>
     * 总结：
     * Spring-Cache仅仅只解决了我们读模式的一些问题，但是对于写模式没有做相应的处理
     * 常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用Spring-Cache）
     * 写模式的话，我们注意只要缓存的数据有过期时间就足够了，再高级点就加上读写锁就行
     * 特殊数据：特殊设计
     * <p>
     * 原理：
     * CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     */
    // 代表当前方法的结果需要缓存
    // 如果缓存中有，方法不用调用，如果缓存中没有，会调用方法，最后将方法的结果放入缓存
    // 注意每一个需要缓存的数据
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        System.out.println("getLevel1Categorys........");
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> categoryEntities = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return categoryEntities;
        //  return this.baseMapper.selectList(
        //          new QueryWrapper<CategoryEntity>().eq("parent_cid", parentCid));
    }


    // 注意这里的value是缓存分区，和我们nacos的命名空间很类似
    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2VO>> getCatalogJson() {
        //  将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        //  1、查出所有分类
        //  1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        //  封装数据
        Map<String, List<Catelog2VO>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            //  2、封装上面的结果
            List<Catelog2VO> Catelog2VOs = null;
            if (categoryEntities != null) {
                Catelog2VOs = categoryEntities.stream().map(l2 -> {
                    Catelog2VO Catelog2VO = new Catelog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    //  1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2VO.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            //  2、封装成指定格式
                            Catelog2VO.Category3Vo category3Vo = new Catelog2VO.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        Catelog2VO.setCatalog3List(category3Vos);
                    }

                    return Catelog2VO;
                }).collect(Collectors.toList());
            }

            return Catelog2VOs;
        }));

        return parentCid;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        //  1.搜集当前节点的ID
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        //  通过递归来查找
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //  递归查找所有菜单的子菜单
    //  这边理解的话我们可以用树的角度来理解
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream()
                //  过滤，将all中所有的实体类的ID和我们传递的item的ID进行比对，留下ID相等的实体类
                .filter(categoryEntity -> Objects.equals(categoryEntity.getParentCid(), root.getCatId()))
                //  找到子菜单
                //  可能会有多个层级，我们这里要采用递归，一直得到树最底层的children
                .peek(categoryEntity -> categoryEntity.setChildren(getChildren(categoryEntity, all)))
                //  对children依次排序
                .sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort())))
                //  将最后获取到的流封装成一个list
                .collect(Collectors.toList());
    }

    //  TODO 产生堆外内存溢出OutOfDirectMemoryError:
    //  1)、springboot2.0以后默认使用lettuce操作redis的客户端，它使用通信
    //  2)、lettuce的bug导致netty堆外内存溢出   可设置：-Dio.netty.maxDirectMemory
    //  解决方案：不能直接使用-Dio.netty.maxDirectMemory去调大堆外内存
    //  1)、升级lettuce客户端。      2）、切换使用jedis
    //  @Override
    public Map<String, List<Catelog2VO>> getCatalogJson2() {
        // 给缓存中放json字符串，拿出的json字符串，反序列为能用的对象

        /**
         * 1、空结果缓存：解决缓存穿透问题
         * 2、设置过期时间(加随机值)：解决缓存雪崩
         * 3、加锁：解决缓存击穿问题
         */

        //  1、加入缓存逻辑,缓存中存的数据是json字符串
        //  JSON跨语言。跨平台兼容。
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String catalogJson = ops.get("catalogJson");
        if (StringUtils.isEmpty(catalogJson)) {
            System.out.println("缓存不命中...查询数据库...");
            // 2、缓存中没有数据，查询数据库
            Map<String, List<Catelog2VO>> catalogJsonFromDb = getCatalogJsonFromDbWithRedissonLock();

            return catalogJsonFromDb;
        }

        System.out.println("缓存命中...直接返回...");
        // 转为指定的对象
        Map<String, List<Catelog2VO>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2VO>>>() {
        });

        return result;
    }


    /**
     * 缓存里的数据如何和数据库的数据保持一致？？
     * 缓存数据一致性
     * 1)、双写模式
     * 2)、失效模式
     */

    public Map<String, List<Catelog2VO>> getCatalogJsonFromDbWithRedissonLock() {

        //  1、占分布式锁。去redis占坑
        //  （锁的粒度，越细越快:具体缓存的是某个数据，11号商品） product-11-lock
        //  RLock catalogJsonLock = redissonClient.getLock("catalogJson-lock");
        //  创建读锁
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("catalogJson-lock");

        RLock rLock = readWriteLock.readLock();

        Map<String, List<Catelog2VO>> dataFromDb = null;
        try {
            rLock.lock();
            // 加锁成功...执行业务
            dataFromDb = getDataFromDb();
        } finally {
            rLock.unlock();
        }
        //  先去redis查询下保证当前的锁是自己的
        //  获取值对比，对比成功删除=原子性 lua脚本解锁
        //   String lockValue = stringRedisTemplate.opsForValue().get("lock");
        //   if (uuid.equals(lockValue)) {
        //       // 删除我自己的锁
        //       stringRedisTemplate.delete("lock");
        //   }

        return dataFromDb;

    }


    /**
     * 从数据库查询并封装数据::分布式锁
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonFromDbWithRedisLock() {

        //  1、占分布式锁。去redis占坑      设置过期时间必须和加锁是同步的，保证原子性（避免死锁）
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            Map<String, List<Catelog2VO>> dataFromDb = null;
            try {
                //  加锁成功...执行业务
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                //  删除锁
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);

            }
            // 先去redis查询下保证当前的锁是自己的
            // 获取值对比，对比成功删除=原子性 lua脚本解锁
            //  String lockValue = stringRedisTemplate.opsForValue().get("lock");
            //  if (uuid.equals(lockValue)) {
            //      // 删除我自己的锁
            //      stringRedisTemplate.delete("lock");
            //  }

            return dataFromDb;
        } else {
            System.out.println("获取分布式锁失败...等待重试...");
            // 加锁失败...重试机制
            // 休眠一百毫秒
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDbWithRedisLock();     // 自旋的方式
        }
    }

    private Map<String, List<Catelog2VO>> getDataFromDb() {
        // 得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson)) {
            // 缓存不为空直接返回
            Map<String, List<Catelog2VO>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2VO>>>() {
            });

            return result;
        }

        System.out.println("查询了数据库");

        /**
         * 将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = this.baseMapper.selectList(null);

        // 1、查出所有分类
        // 1、1）查出所有一级分类
        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L);

        // 封装数据
        Map<String, List<Catelog2VO>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 1、每一个的一级分类,查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParent_cid(selectList, v.getCatId());

            // 2、封装上面的结果
            List<Catelog2VO> Catelog2VOs = null;
            if (categoryEntities != null) {
                Catelog2VOs = categoryEntities.stream().map(l2 -> {
                    Catelog2VO Catelog2VO = new Catelog2VO(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName().toString());

                    // 1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParent_cid(selectList, l2.getCatId());

                    if (level3Catelog != null) {
                        List<Catelog2VO.Category3Vo> category3Vos = level3Catelog.stream().map(l3 -> {
                            // 2、封装成指定格式
                            Catelog2VO.Category3Vo category3Vo = new Catelog2VO.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());

                            return category3Vo;
                        }).collect(Collectors.toList());
                        Catelog2VO.setCatalog3List(category3Vos);
                    }

                    return Catelog2VO;
                }).collect(Collectors.toList());
            }

            return Catelog2VOs;
        }));

        // 3、将查到的数据放入缓存,将对象转为json
        String valueJson = JSON.toJSONString(parentCid);
        stringRedisTemplate.opsForValue().set("catalogJson", valueJson, 1, TimeUnit.DAYS);

        return parentCid;
    }

    /**
     * 从数据库查询并封装数据::本地锁
     */
    public Map<String, List<Catelog2VO>> getCatalogJsonFromDbWithLocalLock() {

        //  // 如果缓存中有就用缓存的
        //  Map<String, List<Catelog2VO>> catalogJson = (Map<String, List<Catelog2VO>>) cache.get("catalogJson");
        //  if (cache.get("catalogJson") == null) {
        //      // 调用业务
        //      // 返回数据又放入缓存
        //  }

        // 只要是同一把锁，就能锁住这个锁的所有线程
        // 1、synchronized (this)：SpringBoot所有的组件在容器中都是单例的。
        // TODO 本地锁：synchronized，JUC（Lock),在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {

            // 得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }
    }

}