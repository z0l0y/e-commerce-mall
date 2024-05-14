package com.github.ecommercemall.ecommercemallorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.common.exception.NoStockException;
import com.github.common.to.OrderCreateTo;
import com.github.common.utils.PageUtils;
import com.github.common.utils.Query;
import com.github.common.utils.R;
import com.github.common.vo.MemberResponseVo;
import com.github.ecommercemall.ecommercemallorder.dao.OrderDao;
import com.github.ecommercemall.ecommercemallorder.entity.OrderEntity;
import com.github.ecommercemall.ecommercemallorder.feign.WmsFeignService;
import com.github.ecommercemall.ecommercemallorder.interceptor.LoginUserInterceptor;
import com.github.ecommercemall.ecommercemallorder.service.OrderItemService;
import com.github.ecommercemall.ecommercemallorder.service.OrderService;
import com.github.ecommercemall.ecommercemallorder.vo.OrderItemVo;
import com.github.ecommercemall.ecommercemallorder.vo.OrderSubmitVo;
import com.github.ecommercemall.ecommercemallorder.vo.SubmitOrderResponseVo;
import com.github.ecommercemall.ecommercemallorder.vo.WareSkuLockVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.common.constant.CartConstant.CART_PREFIX;
import static com.github.common.constant.OrderConstant.USER_ORDER_TOKEN_PREFIX;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> confirmVoThreadLocal = new ThreadLocal<>();

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WmsFeignService wmsFeignService;

    @Autowired
    private OrderItemService orderItemService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

        return new PageUtils(page);
    }

    /**
     * 提交订单
     *
     * @param vo
     * @return
     */
    /**
     * 事务的隔离级别:
     *  READ UNCOMMITTED（读未提交）
     * 该隔离级别的事务会读到其它未提交事务的数据，此现象也称之为脏读。 READ COMMITTED（读提交）
     * 一个事务可以读取另一个已提交的事务，多次读取会造成不一样的结果，此现象称为不可重
     * 复读问题，Oracle 和 SQL Server 的默认隔离级别。
     *  READ COMMITTED（读提交）
     * 一个事务可以读取另一个已提交的事务，多次读取会造成不一样的结果，此现象称为不可重
     * 复读问题，Oracle 和 SQL Server 的默认隔离级别。
     *  REPEATABLE READ（可重复读）
     * 该隔离级别是 MySQL 默认的隔离级别，在同一个事务里，select 的结果是事务开始时时间
     * 点的状态，因此，同样的 select 操作读到的结果会是一致的，但是，会有幻读现象。MySQL
     * 的 InnoDB 引擎可以通过 next-key locks 机制（参考下文"行锁的算法"一节）来避免幻读。
     *  SERIALIZABLE（序列化）
     * 在该隔离级别下事务都是串行顺序执行的，MySQL 数据库的 InnoDB 引擎会给读操作隐式
     * 加一把读共享锁，从而避免了脏读、不可重读复读和幻读问题。
     */
    /**
     * 事务的传播行为:
     * 1、PROPAGATION_REQUIRED：如果当前没有事务，就创建一个新事务，如果当前存在事务，
     * 就加入该事务，该设置是最常用的设置。
     * 2、PROPAGATION_SUPPORTS：支持当前事务，如果当前存在事务，就加入该事务，如果当
     * 前不存在事务，就以非事务执行。
     * 3、PROPAGATION_MANDATORY：支持当前事务，如果当前存在事务，就加入该事务，如果
     * 当前不存在事务，就抛出异常。
     * 4、PROPAGATION_REQUIRES_NEW：创建新事务，无论当前存不存在事务，都创建新事务。
     * 5、PROPAGATION_NOT_SUPPORTED：以非事务方式执行操作，如果当前存在事务，就把当
     * 前事务挂起。
     * 6、PROPAGATION_NEVER：以非事务方式执行，如果当前存在事务，则抛出异常。
     * 7、PROPAGATION_NESTED：如果当前存在事务，则在嵌套事务内执行。如果当前没有事务，
     * 则执行与 PROPAGATION_REQUIRED 类似的操作。
     *
     * @param vo
     * @return
     */

    /**
     * @ Transactional(timeout = 30) 设置事务的超时时间，设置只要30秒没有返回就要回滚
     * @ Transactional(isolation = Isolation.READ_COMMITTED) 设置事务的隔离级别
     * @ Transactional(propagation = Propagation.REQUIRED)   设置事务的传播级别，如果在另外一个事务里面，那么就会依赖上一个事务的配置
     * @ Transactional(propagation = Propagation.REQUIRES_NEW) 这个会重新新建一个自己的事务
     * 事务常常和我们的数据库配合使用，只要有一个数据库的操作发生失败，那么全部的数据库操作全部都要回滚，还有就是注意一点这个注解是在Impl包下面使用的
     * <p>
     * 事务是使用代理对象来控制的。在同一个类里面，编写两个方法，内部调用的时候，会导致事务设置失效。原因是没有用到代理对象。
     * <p>
     * 本地事务失效问题：
     * 同一个对象内事务方法互调默认失败，原因是 绕过了代理对象，事务时使用代理对象来控制的
     * 解决方法：
     * 使用代理对象来调用事务方法
     * 解决：
     * 0）、导入 spring-boot-starter-aop,其实就是使用里面的aspectj
     * 1）、@EnableTransactionManagement(proxyTargetClass = true)
     * 2）、@EnableAspectJAutoProxy(exposeProxy=true) 开启aspectj动态代理，以后所有的动态代理都是aspectj创建的
     * （即使没有接口也可以创建动态代理） exposeProxy=true 表示对外暴露代理对象
     * 3）、AopContext.currentProxy() 调用方法。获取到当前类的代理对象，然后还是要通过代理对象来操作事务方法
     * OrderServiceImpl orderServiceImpl = (OrderServiceImpl)AopContext.currentProxy();
     * orderServiceImpl.A();
     * orderServiceImpl.B();
     */

    /**
     * CAP 定理
     * CAP 原则又称 CAP 定理，指的是在一个分布式系统中
     *  一致性（Consistency）：
     *  在分布式系统中的所有数据备份，在同一时刻是否同样的值。（等同于所有节点访
     * 问同一份最新的数据副本）
     *  可用性（Availability）
     *  在集群中一部分节点故障后，集群整体是否还能响应客户端的读写请求。（对数据
     * 更新具备高可用性）
     *  分区容错性（Partition tolerance）
     *  大多数分布式系统都分布在多个子网络。每个子网络就叫做一个区（partition）。
     * 分区容错的意思是，区间通信可能失败。比如，一台服务器放在中国，另一台服务
     * 器放在美国，这就是两个区，它们之间可能无法通信。
     * CAP 原则指的是，这三个要素最多只能同时实现两点，不可能三者兼顾。
     * <p>
     * 一般来说，分区容错无法避免，因此可以认为 CAP 的 P 总是成立。CAP 定理告诉我们，
     * 剩下的 C 和 A 无法同时做到。
     * 一般只有CP和AP，CA的话就是在本地访问，我们目前不讨论这种情况
     * <p>
     * Raft算法演示 <a href="http://thesecretlivesofdata.com/raft/"></a>
     * 任何一个节点都有三种状态：
     * Follower Candidate Leader
     * 该算法发核心是 Leader Election 领导选举
     * 本质上还是选举一个性能最好的作为领导，因为到时候多个候选者竞争的时候就看谁给Follower发请求发的快
     * 还是我们熟悉的心跳 ping pong 和Redis和nacos很类似，不过还有点集群双主选举的味道
     * 一个挂了，不发送ping pong了，那么会选举一个新的Leader，这时候看谁的自旋时间更短
     * 注意一个节点在一轮选举中只能投票一次，而且只会选举一个Leader，所以如果有两个候选者的票数一样的话会进入到下一轮的投票再进行选举
     * Log Replication 日志复制，注意日志是在心跳的时候发出去的，而且是一去一回才会使集群的数据同步更新
     * 同时该算法也实现了分区容错性
     */

    /**
     * BASE 理论
     * 是对 CAP 理论的延伸，思想是即使无法做到强一致性（CAP 的一致性就是强一致性），但可
     * 以采用适当的采取弱一致性，即最终一致性。
     * BASE 是指
     *  基本可用（Basically Available）
     *  基本可用是指分布式系统在出现故障的时候，允许损失部分可用性（例如响应时间、
     * 功能上的可用性），允许损失部分可用性。需要注意的是，基本可用绝不等价于系
     * 统不可用。
     *  响应时间上的损失：正常情况下搜索引擎需要在 0.5 秒之内返回给用户相应的
     * 查询结果，但由于出现故障（比如系统部分机房发生断电或断网故障），查询
     * 结果的响应时间增加到了 1~2 秒。
     *  功能上的损失：购物网站在购物高峰（如双十一）时，为了保护系统的稳定性，
     * 部分消费者可能会被引导到一个降级页面。
     *  软状态（ Soft State）
     *  软状态是指允许系统存在中间状态，而该中间状态不会影响系统整体可用性。分布
     * 式存储中一般一份数据会有多个副本，允许不同副本同步的延时就是软状态的体
     * 现。mysql replication 的异步复制也是一种体现。
     *  最终一致性（ Eventual Consistency）
     *  最终一致性是指系统中的所有数据副本经过一定时间后，最终能够达到一致的状
     * 态。弱一致性和强一致性相反，最终一致性是弱一致性的一种特殊情况。
     * <p>
     * 强一致性、弱一致性、最终一致性
     * 从客户端角度，多进程并发访问时，更新过的数据在不同进程如何获取的不同策略，决定了
     * 不同的一致性。对于关系型数据库，要求更新过的数据能被后续的访问都能看到，这是强一
     * 致性。如果能容忍后续的部分或者全部访问不到，则是弱一致性。如果经过一段时间后要求
     * 能访问到更新后的数据，则是最终一致性
     */
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    //  @GlobalTransactional(rollbackFor = Exception.class)
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {

        confirmVoThreadLocal.set(vo);

        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        // 去创建、下订单、验令牌、验价格、锁定库存...

        // 获取当前用户登录的信息
        MemberResponseVo memberResponseVo = LoginUserInterceptor.loginUser.get();
        responseVo.setCode(0);

        // 1、验证令牌是否合法【令牌的对比和删除必须保证原子性】
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        String orderToken = vo.getOrderToken();

        // 通过lua脚本原子验证令牌和删除令牌
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), orderToken);

        if (result == 0L) {
            // 令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        } else {
            // 令牌验证成功
            // 1、创建订单、订单项等信息
            // OrderCreateTo order = createOrder();
            OrderCreateTo order = null;
            // 2、验证价格
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();

            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 金额对比
                // TODO 3、保存订单
                // saveOrder(order);

                // 4、库存锁定,只要有异常，回滚订单数据
                // 订单号、所有订单项信息(skuId,skuNum,skuName)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                // 获取出要锁定的商品数据信息
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);

                // TODO 调用远程锁定库存的方法
                // 出现的问题：扣减库存成功了，但是由于网络原因超时，出现异常，导致订单事务回滚，库存事务不回滚(解决方案：seata)
                // 为了保证高并发，不推荐使用seata，因为是加锁，并行化，提升不了效率,可以发消息给库存服务
                /**
                 * 最后我们采取的是这样的策略
                 * 1
                 * 柔性事务-最大努力通知型方案:
                 * 按规律进行通知，不保证数据一定能通知成功，但会提供可查询操作接口进行核对。这种
                 * 方案主要用在与第三方系统通讯时，比如：调用微信或支付宝支付后的支付结果通知。这种
                 * 方案也是结合 MQ 进行实现，例如：通过 MQ 发送 http 请求，设置最大通知次数。达到通
                 * 知次数后即不再通知。
                 * 案例：银行通知、商户通知等（各大交易业务平台间的商户通知：多次通知、查询校对、对
                 * 账文件），支付宝的支付成功异步回调
                 * 2
                 * 柔性事务-可靠消息+最终一致性方案（异步确保型）:
                 * 实现：业务处理服务在业务事务提交之前，向实时消息服务请求发送消息，实时消息服务只
                 * 记录消息数据，而不是真正的发送。业务处理服务在业务事务提交之后，向实时消息服务确
                 * 认发送。只有在得到确认发送指令后，实时消息服务才会真正发送。
                 */
                R r = wmsFeignService.orderLockStock(lockVo);
                if (r.getCode() == 0) {
                    // 锁定成功
//                    responseVo.setOrder(order.getOrder());
                    //  int i = 10/0;

                    // TODO 订单创建成功，发送消息给MQ，这里给订单定时还挺符合我们生活中的实际情况的
                    //  想想我们在淘宝，京东上买东西的时候，超过24小时订单自动取消，使用的就是延迟队列
                    rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());

                    // 删除购物车里的数据
                    redisTemplate.delete(CART_PREFIX + memberResponseVo.getId());
                    return responseVo;
                } else {
                    // 锁定失败
                    String msg = (String) r.get("msg");
                    throw new NoStockException(msg);
                    //  responseVo.setCode(3);
                    //  return responseVo;
                }

            } else {
                responseVo.setCode(2);
                return responseVo;
            }
        }
    }

//    /**
//     * 保存订单所有数据
//     * @param orderCreateTo
//     */
//    private void saveOrder(OrderCreateTo orderCreateTo) {
//        // 获取订单信息
//        OrderEntity order = orderCreateTo.getOrder();
//        order.setModifyTime(new Date());
//        order.setCreateTime(new Date());
//        //保存订单
//        this.baseMapper.insert(order);
//
//        //获取订单项信息
//        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
//        //批量保存订单项数据
//        orderItemService.saveBatch(orderItems);
//    }
//
//    private OrderCreateTo createOrder() {
//
//        OrderCreateTo createTo = new OrderCreateTo();
//
//        //1、生成订单号
//        String orderSn = IdWorker.getTimeId();
//        OrderEntity orderEntity = builderOrder(orderSn);
//
//        //2、获取到所有的订单项
//        List<OrderItemEntity> orderItemEntities = builderOrderItems(orderSn);
//
//        //3、验价(计算价格、积分等信息)
//        computePrice(orderEntity,orderItemEntities);
//
//        createTo.setOrder(orderEntity);
//        createTo.setOrderItems(orderItemEntities);
//
//        return createTo;
//    }

}