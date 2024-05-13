package com.github.ecommercemall.ecommercemallproduct;

import com.github.ecommercemall.ecommercemallproduct.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.ws.Action;
import java.util.Arrays;
import java.util.UUID;

// 搞半天是这个注解没有写上去，所以categoryService默认是null，会导致空指针异常
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class ECommerceMallProductApplicationTests {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test() {
        val catelogPath = categoryService.findCatelogPath(225L);
        log.info("{}", Arrays.asList(catelogPath));
    }

    @Test
    public void contextLoads() {

    }

    @Test
    public void testStringRedis() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        //保存
        ops.set("hello","world_" + UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");
        System.out.println("之前保存的数据:"+hello);
    }

    @Test
    public void testRedisson() {
        System.out.println(redissonClient);
    }
}
