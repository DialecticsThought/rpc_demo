package com.rpc.rpc_demo.fault.retry;

import com.google.common.base.Stopwatch;
import com.rpc.rpc_demo.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @Description 线性重试策略
 * <pre>
 * 这个实现与 指数退避重试策略 非常相似, 主要区别在于退避时间的计算方式:
 *      在每次重试时,我们将退避时间 backoffInterval 线性增加,初始值为 1 秒。
 *      具体计算方式为 backoffInterval += INITIAL_BACKOFF_INTERVAL。这样每次重试时,退避时间都会增加 1 秒。
 *      其他部分,如最大重试次数、异常处理、日志记录等,与指数退避重试策略保持一致
 * 这种线性重试策略适用于网络环境相对较为稳定的场景,对响应时间要求也不太严格。
 * 它能够提供一个平滑的重试过程,不会像指数退避那样导致重试间隔时间过长。
 * 与指数退避相比,线性重试的优点是:
 *      响应时间更短:每次重试的时间间隔增长较缓慢,可以更快地得到服务响应。
 *      更加稳定:重试间隔变化平缓,不会出现大幅波动。
 * 缺点是:
 *      对网络抖动不太敏感:当网络环境较差时,线性重试可能无法有效地抑制重试请求
 * </pre>
 * @Author veritas
 * @Data 2025/3/9 16:01
 */
@Slf4j
public class LinearRetryStrategy implements RetryStrategy {
    // 定义最大重试次数为5次
    private static final int MAX_RETRY_TIMES = 5;
    // 定义初始退避间隔为1000毫秒（1秒）
    private static final long INITIAL_BACKOFF_INTERVAL = 1000;

    /**
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 初始化当前重试次数为0
        int retryTimes = 0;
        // 初始化退避间隔为初始值，即1秒
        long backOffInterval = INITIAL_BACKOFF_INTERVAL;
        // 创建一个未启动的Stopwatch计时器，用于测量每次调用的耗时
        Stopwatch stopwatch = Stopwatch.createStarted();
        // 当重试次数小于最大重试次数时，进入重试循环
        while (retryTimes < MAX_RETRY_TIMES) {
            try {
                // 开始计时，记录当前调用开始的时间
                stopwatch.start();
                // 调用传入的Callable任务，如果成功，则直接返回结果
                return callable.call();
            } catch (Exception e) {
                // 如果任务调用抛出异常，则捕获异常
                // 增加重试次数
                retryTimes++;
                // 记录警告日志，包含当前重试次数和异常信息
                log.warn("RPC call failed,retrying.. current retry times:{}", retryTimes, e);
                // 停止计时，记录本次调用结束的时间
                stopwatch.stop();
                // 获取调用过程中消耗的时间（毫秒）
                long elapsedTime = stopwatch.elapsed(TimeUnit.MICROSECONDS);
                // 剩余的等待时间 = 退避间隔 - 减去任务调用所消耗的时间
                long restTime = backOffInterval - elapsedTime;
                // 计算睡眠时间
                long sleepTime = Math.min(backOffInterval, Math.max(0, restTime));
                // 重置计时器，为下一次重试做准备
                stopwatch.reset();
                if (sleepTime > 0) {
                    // 记录信息日志，显示将等待的毫秒数
                    log.info("Backing off for {} ms before next retry.", sleepTime);
                    // 线程睡眠指定的时间，暂停后再进行下一次重试
                    Thread.sleep(sleepTime);
                }
                // 线性增加退避时间：每次重试后将退避间隔增加初始间隔值
                backOffInterval = backOffInterval + INITIAL_BACKOFF_INTERVAL;
            }
        }
        // 如果重试次数达到上限仍未成功，则抛出异常，提示重试次数已超出限制
        throw new Exception("Maximum retry times exceeded, giving up.");
    }
}
