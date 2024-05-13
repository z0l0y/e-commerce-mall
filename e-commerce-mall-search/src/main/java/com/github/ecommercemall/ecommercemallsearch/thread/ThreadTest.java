package com.github.ecommercemall.ecommercemallsearch.thread;

import java.util.concurrent.*;

public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 初始化线程的 4 种方式:
         * 1）、继承 Thread
         *     System.out.println("main......start.....");
         *     Thread thread = new Thread01();
         *     thread.start();// 启动线程
         *     System.out.println("main......end.....");
         * 2）、实现 Runnable 接口
         *     Runable01 runable01 = new Runable01();
         *     new Thread(runable01).start();
         * 3）、实现 Callable 接口 + FutureTask （可以拿到返回结果，可以处理异常）
         *     FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *     new Thread(futureTask).start();
         *     // 阻塞等待整个线程执行完成，获取返回结果
         *     System.out.println(futureTask.get());
         * 4）、线程池
         *     // 线程池直接提交任务
         *     public static ExecutorService executor = Executors.newFixedThreadPool(10);
         *     service.execute(new Runable01());
         *     Future<Integer> submit = service.submit(new Callable01());
         *     submit.get();
         * 方式 1 和方式 2：主进程无法获取线程的运算结果。不适合当前场景
         * 方式 3：主进程可以获取线程的运算结果，但是不利于控制服务器中的线程资源。可以导致
         * 服务器资源耗尽。
         * 方式 4：通过如下两种方式初始化线程池
         *     Executors.newFixedThreadPool(3);
         * 或者
         *     new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit unit,
         *     workQueue, threadFactory, handler);
         * 1，2，3都不能控制资源
         * 4可以控制资源，性能稳定
         *
         * 总结：
         * 我们以后在业务代码里面，以上三种启动线程的方式都不用，将所有的多线程异步任务都交给线程池来执行
         *
         * 线程池的七大参数:
         * corePoolSize【5】：核心线程数，线程池，创建好以后就准备就绪的线程数量，就等待来接受异步任务去执行
         *  5个  Thread thread = new Thread();
         * 池中一直保持的线程的数量，即使线程空闲。除非设置了 allowCoreThreadTimeOut、
         * * @param corePoolSize the number of threads to keep in the pool, even
         * * if they are idle, unless {@code allowCoreThreadTimeOut} is set
         * maximumPoolSize: 最大线程数量，控制资源，实现线程资源的再利用
         * 池中允许的最大的线程数，；类似于正式员工
         * * @param maximumPoolSize the maximum number of threads to allow in the
         * * pool
         * keepAliveTime：类似于是招的零时工，忙的时候就要，不忙的时候就释放（解雇）了，释放的空闲线程为（maximumPoolSize-corePoolSize）
         * 当线程数大于核心线程数的时候，线程在最大多长时间没有接到新任务就会终止释放，
         * 最终线程池维持在 corePoolSize 大小
         * * @param keepAliveTime when the number of threads is greater than
         * * the core, this is the maximum time that excess idle threads
         * * will wait for new tasks before terminating.
         * unit：存活时间的单位
         * 时间单位
         * * @param unit the time unit for the {@code keepAliveTime} argument
         * workQueue：类似于计网的接收队列，是生产者，等待我们的线程（消费者）来取任务，队列主要存放目前多出来的任务
         * 只要有线程空闲，就会去队列里面取出新的任务继续执行
         * 阻塞队列，用来存储等待执行的任务，如果当前对线程的需求超过了 corePoolSize
         * 大小，就会放在这里等待空闲线程执行。
         * * @param workQueue the queue to use for holding tasks before they are
         * * executed. This queue will hold only the {@code Runnable}
         * * tasks submitted by the {@code execute} method.
         * threadFactory：
         * 创建线程的工厂，比如指定线程名等
         * * @param threadFactory the factory to use when the executor
         * * creates a new thread
         * handler：
         * 拒绝策略，如果线程满了，线程池就会使用拒绝策略。
         * * @param handler the handler to use when execution is blocked
         * * because the thread bounds and queue capacities are reached
         *
         * 运行流程：
         * 1、线程池创建，准备好 core 数量的核心线程，准备接受任务
         * 2、新的任务进来，用 core 准备好的空闲线程执行。
         * (1) 、core 满了，就将再进来的任务放入阻塞队列中。空闲的 core 就会自己去阻塞队
         * 列获取任务执行
         * (2) 、阻塞队列满了，就直接开新线程执行，最大只能开到 max 指定的数量
         * (3。1) 、max 都执行好了。Max-core 数量空闲的线程会在 keepAliveTime 指定的时间后自
         * 动销毁。最终保持到 core 大小
         * (3.2) 、如果线程数开到了 max 的数量，还有新任务进来.就会使用 reject 指定的拒绝策
         * 略进行处理
         * 3、所有的线程创建都是由指定的 factory 创建的。
         */
        // System.out.println("main......start.....");
        // 1、runXxxx 都是没有返回结果的，supplyXxx 都是可以获取返回结果的
        // 2、可以传入自定义的线程池，否则就用默认的线程池；
        // CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        // }, executor);

        /**
         * 方法完成后的感知:
         * whenComplete 可以处理正常和异常的计算结果，exceptionally 处理异常情况。
         * whenComplete 和 whenCompleteAsync 的区别：
         *  whenComplete：是执行当前任务的线程执行继续执行 whenComplete 的任务。
         *  whenCompleteAsync：是执行把 whenCompleteAsync 这个任务继续提交给线程池
         *  来进行执行。
         * 方法不以 Async 结尾，意味着 Action 使用相同的线程执行，而 Async 可能会使用其他线程
         * 执行（如果是使用相同的线程池，也可能会被同一个线程选中执行）
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 0;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, executor).whenComplete((res,exception) -> {
        //     // 虽然能得到异常信息，但是没法修改返回数据
        //     System.out.println("异步任务成功完成了...结果是：" + res + "异常是：" + exception);
        // }).exceptionally(throwable -> {
        //     // 可以感知异常，同时返回默认值
        //     return 10;
        // });

        /**
         * 方法执行完后的处理
         * handle 和 complete 一样，可对结果做最后的处理（可处理异常），可改变返回值。
         */
        // CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
        //     System.out.println("当前线程：" + Thread.currentThread().getId());
        //     int i = 10 / 2;
        //     System.out.println("运行结果：" + i);
        //     return i;
        // }, executor).handle((result,thr) -> {
        //     if (result != null) {
        //         return result * 2;
        //     }
        //     if (thr != null) {
        //         System.out.println("异步任务成功完成了...结果是：" + result + "异常是：" + thr);
        //         return 0;
        //     }
        //     return 0;
        // });


        /**
         * 线程串行化:
         * 1、thenRun：不能获取上一步的执行结果
         * .thenRun(() -> {
         *             System.out.println("任务2启动了..." + res);
         *         }, executor);
         * 2、thenAcceptAsync：能接受上一步结果，但是无返回值
         * .thenAcceptAsync(res -> {
         *             System.out.println("任务2启动了..." + res);
         *         }, executor);
         * 3、thenApplyAsync：能接受上一步结果，有返回值
         * .thenApplyAsync(res -> {
         *             System.out.println("任务2启动了..." + res);
         *             return "Hello" + res;
         *         }, executor);
         * 任务的返回值。
         *
         * thenAccept 方法：消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
         * thenRun 方法：只要上面的任务执行完成，就开始执行 thenRun，只是处理完任务后，执行
         * thenRun 的后续操作
         * 带有 Async(异步) 默认是异步执行的。同之前。
         *
         * 以上都要前置任务成功完成。
         * Function<? super T,? extends U>
         * T：上一个任务返回结果的类型
         * U：当前任务的返回值类型
         *
         */

//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//            // 可以看到是有res返回值的
//        }, executor).thenApplyAsync(res -> {
//            System.out.println("任务2启动了..." + res);
//            return "Hello" + res;
//        }, executor);
//        System.out.println("main......end....." + future.get());

        /**
         * 两任务组合 - 都要完成:
         * 两个任务必须都完成，触发该任务。
         * thenCombine：组合两个 future，获取两个 future 的返回结果，并返回当前任务的返回值
         * thenAcceptBoth：组合两个 future，获取两个 future 任务的返回结果，然后处理任务，没有
         * 返回值。
         * runAfterBoth：组合两个 future，不需要获取 future 的结果，只需两个 future 处理完任务后，
         * 处理该任务
         */
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务线程1：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结束：" + i);
            return i;
        }, executor);

        // 注意要有线程池，要不然使用默认的线程池，主线程执行太快了会导致有一些线程的任务执行不完就结束了
        // 但是我们自己的线程池不会出现这样的问题，它一直都存在
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务线程2：" + Thread.currentThread().getId());
            System.out.println("任务线程2结束");
            return "Hello";
        }, executor);

//        future1.thenAcceptBothAsync(future2, (res1, res2) -> {
//            System.out.println("任务3开始执行 " + Thread.currentThread().getId());
//        }, executor);

//        CompletableFuture<String> stringCompletableFuture = future1.thenCombineAsync(future2, (res1, res2) -> {
//            System.out.println("任务3开始执行 " + Thread.currentThread().getId());
//            return res1 + " , " + res2;
//        }, executor);
//
//        System.out.println(stringCompletableFuture.get());

        /**
         * 两任务组合 - 一个完成:
         * 当两个任务中，任意一个 future 任务完成的时候，执行任务。
         * applyToEither：两个任务有一个执行完成，获取它的返回值，处理任务并有新的返回值。
         * acceptEither：两个任务有一个执行完成，获取它的返回值，处理任务，没有新的返回值。
         * runAfterEither：两个任务有一个执行完成，不需要获取 future 的结果，处理任务，也没有返回值
         */
//        future1.runAfterEitherAsync(future2, () -> {
//            System.out.println("任务三开始执行");
//        }, executor);

        /**
         * 多任务组合:
         * allOf：等待所有任务完成
         * anyOf：只要有一个任务完成
         */
        Void unused = CompletableFuture.allOf(future1, future2).get();
        System.out.println(future1.get() + future2.get());

        Object result = CompletableFuture.anyOf(future1, future2).get();
        System.out.println(result);

    }

    private static void threadPool() {

        /**
         * 一个线程池 core 7； max 20 ，queue：50，100 并发进来怎么分配的；
         * 先有 7 个能直接得到执行，接下来 50 个进入队列排队，在多开 13 个继续执行。现在 70 个
         * 被安排上了。剩下 30 个默认拒绝策略。
         */
        ExecutorService threadPool = new ThreadPoolExecutor(
                200,
                10,
                10L,
                TimeUnit.SECONDS,
                // new LinkedBlockingDeque<Runnable>() 默认容量是Integer的最大值，如果不设置大小会导致内存不够
                new LinkedBlockingDeque<Runnable>(10000),
                Executors.defaultThreadFactory(),
                // 直接将新的任务抛弃，并且抛出异常
                new ThreadPoolExecutor.AbortPolicy()
        );

        // Executors.newCachedThreadPool core是0，所有的都是零时工，所有线程都可以被回收
        // Executors.newFixedThreadPool core是固定大小，都不可回收
        // Executors.newScheduledThreadPool 专门做定时任务的线程池
        // Executors.newSingleThreadExecutor 单线程的线程池，从队列里面获取一个一个任务挨个执行
        // 定时任务的线程池
        /**
         * 开发中为什么使用线程池
         *  降低资源的消耗
         *  通过重复利用已经创建好的线程降低线程的创建和销毁带来的损耗
         *  提高响应速度
         *  因为线程池中的线程数没有超过线程池的最大上限时，有的线程处于等待分配任务
         * 的状态，当任务来时无需创建新的线程就能执行
         *  提高线程的可管理性
         *  线程池会根据当前系统特点对池内的线程进行优化处理，减少创建和销毁线程带来
         * 的系统开销。无限的创建和销毁线程不仅消耗系统资源，还降低系统的稳定性，使
         * 用线程池进行统一分配
         */
        ExecutorService service = Executors.newScheduledThreadPool(2);
    }


    public static class Thread01 extends Thread {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }


    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }

}
