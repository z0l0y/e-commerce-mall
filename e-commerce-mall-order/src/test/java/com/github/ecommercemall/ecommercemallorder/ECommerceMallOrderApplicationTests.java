package com.github.ecommercemall.ecommercemallorder;

import com.github.ecommercemall.ecommercemallorder.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ECommerceMallOrderApplicationTests {

    /**
     * 1. 引入 spring-boot-starter-amqp
     * 2. application.yml配置
     * 3. 测试RabbitMQ
     * 1. AmqpAdmin：管理组件
     * 2. RabbitTemplate：消息发送处理组件
     * 3. @RabbitListener 监听消息的方法可以有三种参数（不分数量，顺序）
     * • Object content, Message message, Channel channel
     */

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void sendMessageTest() {
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(1L);
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setName("reason");
        reasonEntity.setStatus(1);
        reasonEntity.setSort(2);
        String msg = "Hello World";
        // 1、发送消息,如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable接口

        // 2、发送的对象类型的消息，可以是一个json
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello2.java", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
        log.info("消息发送完成:{}", reasonEntity);
    }

    /**
     * 1、如何创建Exchange、Queue、Binding
     * 1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     * <p>
     * 逆天了，如果我们没有事先创建好交换机，队列，那么就算我们本次执行没有用到还是会报错（这个奇怪的Bug是通过控制变量法发现的
     * ，控到最后发现有关rabbitmq的代码只留下createExchange的时候就可以正常运行了。  还以为是我们docker的问题。）
     */
    @Test
    public void createExchange() {
        /**
         *  public DirectExchange(String name, boolean durable, boolean autoDelete) {
         *         super(name, durable, autoDelete);
         *     }
         */
        Exchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功：", "hello-java-exchange");
    }


    @Test
    public void testCreateQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：", "hello-java-queue");
    }


    @Test
    public void createBinding() {

        Binding binding = new Binding("hello-java-queue", Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功：", "hello-java-binding");

    }

    @Test
    public void create() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000); // 消息过期时间 1分钟
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：", "order.delay.queue");
    }
}
