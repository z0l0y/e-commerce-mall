package com.github.ecommercemall.ecommercemallorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 使用RabbitMQ
 * 1、引入amqp场景;RabbitAutoConfiguration就会自动生效
 * 2、给容器中自动配置了
 * RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate
 * 所有属性都是 spring.rabbitmq
 *
 * @ ConfigurationProperties(prefix = "spring.rabbitmq")简单的说，就是在我们的配置文件中自己配置好
 * 比如 spring.rabbitmq.host=192.168.56.10 到时候他自己会按照前缀来读取更新配置
 * 3.给配置文件中配置 spring.rabbitmq 信息
 * 4、@EnableRabbit 开启功能
 * 5.监听消息：
 * 使用@RabbitListener;必须有@EnableRabbit注解
 * @ RabbitListener: 用在类 +方法上(指定监听哪些队列即可)
 * @ RabbitHandler: 标在方法上(可以重载区分不同的消息)
 * <p>
 * Seata控制分布式事务
 * 1）、每一个微服务必须创建undo_Log
 * 2）、安装事务协调器：seate-server
 * 3）、整合
 * 1、导入依赖
 * 2、解压并启动seata-server：
 * registry.conf:注册中心配置    修改 registry ： nacos
 * 3、所有想要用到分布式事务的微服务使用seata DataSourceProxy 代理自己的数据源
 * 4、每个微服务，都必须导入
 * registry.conf   file.conf
 * vgroup_mapping.{application.name}-fescar-server-group = "default"
 * 5、启动测试分布式事务
 * 6、给分布式大事务的入口标注 @GlobalTransactional
 * 7、每一个远程的小事务用 @Transactional
 */
@EnableRabbit
@EnableDiscoveryClient
@EnableFeignClients
@MapperScan("com.github.ecommercemall.ecommercemallorder.dao")
@SpringBootApplication
public class ECommerceMallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceMallOrderApplication.class, args);
    }

}
