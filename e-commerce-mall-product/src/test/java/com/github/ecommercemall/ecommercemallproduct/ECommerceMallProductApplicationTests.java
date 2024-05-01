package com.github.ecommercemall.ecommercemallproduct;

import com.github.ecommercemall.ecommercemallproduct.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.ws.Action;
import java.util.Arrays;

// 搞半天是这个注解没有写上去，所以categoryService默认是null，会导致空指针异常
@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest
public class ECommerceMallProductApplicationTests {

    @Autowired
    private CategoryService categoryService;

    @Test
    public void test() {
        val catelogPath = categoryService.findCatelogPath(225L);
        log.info("{}", Arrays.asList(catelogPath));
    }

    @Test
    public void contextLoads() {

    }

}
