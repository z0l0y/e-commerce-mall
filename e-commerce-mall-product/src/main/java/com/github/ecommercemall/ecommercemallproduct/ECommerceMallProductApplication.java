package com.github.ecommercemall.ecommercemallproduct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 缓存常用的注释如下：
 * For caching declaration, Spring’s caching abstraction provides a set of Java annotations:
 *
 * @Cacheable: Triggers cache population.
 * @CacheEvict: Triggers cache eviction.
 * @CachePut: Updates the cache without interfering with the method execution.
 * @Caching: Regroups multiple cache operations to be applied on a method.
 * @CacheConfig: Shares some common cache-related settings at class-level.
 * <p>
 * 缓存常用的注释如下： 对于缓存声明，Spring 的缓存抽象提供了一组 Java 注解：
 * 可缓存：@Cacheable
 * 触发将数据保存到缓存的操作。
 * CacheEvict：@CacheEvict 失效模式
 * 触发删除缓存的操作。
 * 缓存：@CachePut 双写模式
 * 在不干扰方法执行的情况下更新缓存。
 * 缓存：@Caching
 * 重新组合要应用于方法的多个缓存操作，组合以上多个操作。
 * 缓存配置：@CacheConfig
 * 在类级别共享一些与缓存相关的常见设置，在类级别共享缓存的相同配置。
 * <p>
 * 注意在我们的启动类上面写上@EnableCaching这个注解
 * 只用注解就可以实现缓存操作，简化了我们手动的去写缓存的代码
 */
@EnableCaching
@EnableFeignClients(basePackages = "com.github.ecommercemall.ecommercemallproduct.feign")
@MapperScan("com.github.ecommercemall.ecommercemallproduct.dao")
@EnableDiscoveryClient
@SpringBootApplication
public class ECommerceMallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallProductApplication.class, args);
    }

}
