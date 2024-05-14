package com.github.ecommercemall.ecommercemallorder.service.impl;

import com.github.common.exception.NoStockException;
import com.github.common.to.OrderCreateTo;
import com.github.common.utils.R;
import com.github.common.vo.MemberResponseVo;
import com.github.ecommercemall.ecommercemallorder.entity.OrderEntity;
import com.github.ecommercemall.ecommercemallorder.entity.OrderReturnReasonEntity;
import com.github.ecommercemall.ecommercemallorder.feign.WmsFeignService;
import com.github.ecommercemall.ecommercemallorder.interceptor.LoginUserInterceptor;
import com.github.ecommercemall.ecommercemallorder.vo.OrderItemVo;
import com.github.ecommercemall.ecommercemallorder.vo.OrderSubmitVo;
import com.github.ecommercemall.ecommercemallorder.vo.SubmitOrderResponseVo;
import com.github.ecommercemall.ecommercemallorder.vo.WareSkuLockVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;

import com.github.ecommercemall.ecommercemallorder.dao.OrderItemDao;
import com.github.ecommercemall.ecommercemallorder.entity.OrderItemEntity;
import com.github.ecommercemall.ecommercemallorder.service.OrderItemService;
import org.springframework.transaction.annotation.Transactional;

import static com.github.common.constant.CartConstant.CART_PREFIX;
import static com.github.common.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;

//  @RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues：声明需要监听的所有队列
     * <p>
     * org.springframework.amqp.core.Message
     * 参数类型：
     * 1.Message message 原生消息的详细信息，包含了头和体
     * 2.T<发送消息的类型> OrderReturnReasonEntity content;
     * 3.channel：当前传输数据的通道
     * <p>
     * Queue:可以很多人都来监听，只要收到消息，队列就会删除消息，而且只能有一个收到此消息
     * 注意消息是一个一个接收处理的，不是直接接收一坨，而是一个一个的接收
     * <p>
     * 场景：
     * 1) 订单服务启动多个，同一个消息只能有一个客户端收到
     * 2) 只有当一个消息完全处理完，方法运行结束了，我们才可以接收到下一个消息
     */
//     @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {
        //  拿到主体内容
        byte[] body = message.getBody();
        //  拿到的消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        System.out.println("接受到的消息...内容" + message + "===内容：" + content);
        //  该数字deliveryTag在当前通道内是自增的，1，2，3，4，5
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //  签收货物，非批量模式
        try {
            channel.basicAck(deliveryTag, false);
            System.out.println("签收了货物" + deliveryTag);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //  退货,b1为true代表发回服务器,将消息重新入队,b1为false的时候,表示丢弃货物
        try {
            channel.basicReject(deliveryTag, false);
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler
    public void receiveMessage2(Message message,
                                OrderReturnReasonEntity content) {
        //  拿到主体内容
        byte[] body = message.getBody();
        //  拿到的消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        System.out.println("接受到的消息...内容" + message + "===内容：" + content);
    }

}