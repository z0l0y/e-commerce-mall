package com.github.ecommercemall.ecommercemallorder.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {

    private RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /**
     * JSON序列化
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就会回调
     * 1、spring.rabbitmq.publisher-confirms: true
     * 2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     * 1、spring.rabbitmq.publisher-returns: true
     * spring.rabbitmq.template.mandatory: true
     * 2、设置确认回调ReturnCallback
     * <p>
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     * <p>
     * spring.rabbitmq.listener.simple.acknowledge-mode=manual
     * <p>
     * 1.默认是自动确认的，只要消息接收到，客户端会自动确认，服务端就会移除这个消息。
     * 问题：
     * 我们收到很多消息，自动回复给服务器ack，只有一个消息处理成功，如果我们的服务此时宕机了，发生了消息丢失。
     * 方法：
     * 将自动确认配置修改为手动确认。只要我们没有明确告诉MQ，货物被签收，没有ack，那么我们的消息就一直都是unacked状态。
     * 即使是我们的Consumer宕机，消息也不会丢失，而是重置为ready状态，等待新的客户端(Consumer)连接进来，然后再把消息发送给它
     * 2.如何签收
     * channel.basicAck(deliveryTag, false);表示签收，业务完成就可以手动签收
     * channel.basicNack(deliveryTag, false, true);表示拒签，业务处理失败，拒签，可以交给别人处理，或者直接丢弃
     */
    @PostConstruct  // MyRabbitConfig对象创建完成以后，执行这个方法
    public void initRabbitTemplate() {

        /**
         * 1、只要消息抵达Broker就ack=true
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //设置确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("confirm...correlationData[" + correlationData + "]==>ack:[" + ack + "]==>cause:[" + cause + "]");
        });


        /**
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路邮键
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("Fail Message[" + message + "]==>replyCode[" + replyCode + "]" +
                    "==>replyText[" + replyText + "]==>exchange[" + exchange + "]==>routingKey[" + routingKey + "]");
        });
    }
}

