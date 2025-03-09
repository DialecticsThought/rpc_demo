package com.rpc.rpc_demo.fault.retry;

import com.google.common.base.Stopwatch;
import com.rpc.rpc_demo.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @Description 指数退避重试策略
 * 1.定义最大重试次数 MAX_RETRY_TIMES 为 5 次。
 * 2.在 doRetry() 方法中,使用 Stopwatch 来记录每次重试的耗时。
 * 3.在每次重试时,先调用 callable.call() 执行远程调用。
 * 4.如果出现异常,则进行重试处理:
 * 4.1.记录当前重试次数 retryTimes。
 * 4.2.计算本次重试的退避时间 sleepTime。初始退避时间为 100 毫秒,每次重试时退避时间翻倍。
 * 4.3.如果 sleepTime 大于 0,则通过 Thread.sleep() 进行退避延迟。
 * 4.4.如果重试次数达到上限,则抛出异常。
 * 5.如果重试成功,则直接返回结果 RpcResponse。
 * @Author veritas
 * @Data 2025/3/9 15:37
 */
@Slf4j
public class ExponentialBackoffRetryStrategy implements RetryStrategy {
    // 最大重试次数
    private static final int MAX_RETRY_TIMES = 5;
    // 初始退避时间100毫秒
    private static final long INITIAL_BACKOFF_INTERVAL = 100;

    /**
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        // 初始化当前重试次数为0
        int retryTimes = 0;
        // 初始化退避间隔为初始值
        long backOffInterval = INITIAL_BACKOFF_INTERVAL;
        // 创建一个未启动的Stopwatch计时器，用于测量调用时间
        Stopwatch stopwatch = Stopwatch.createStarted();
        //进入循环，最多重试MAX_RETRY_TIMES次
        while (retryTimes < MAX_RETRY_TIMES) {
            try {
                // 开始计时
                stopwatch.start();
                // 调用callable任务，若成功则直接返回结果
                return callable.call();
            } catch (Exception e) {
                // 如果调用抛出异常，则增加重试次数
                retryTimes++;
                log.warn("RPC call failed,retrying.. current retry times: {}", retryTimes, e);
                // 停止计时，记录本次调用花费的时间
                stopwatch.stop();
                // 计算已消耗的时间（毫秒）
                long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
                // 剩余的等待时间 = 退避间隔 - 减去任务调用所消耗的时间
                long restWaitingTime = backOffInterval - elapsedTime;
                // Math.max(0, restWaitingTime) 确保不为负数
                // 如果调用任务的耗时小于退避时间，则等待剩余的时间（即 backoffInterval - elapsedTime）。
                // 如果耗时已经超过或等于退避时间，则无需等待（睡眠时间为 0）
                long sleepTime = Math.min(backOffInterval, Math.max(0, restWaitingTime));
                // 重置计数器 准备下一次计时
                stopwatch.reset();
                // 如果睡眠时间大于0，则进行等待
                if (sleepTime > 0) {
                    log.info("Backing off for {} ms before next retry.", sleepTime);
                    // 线程睡眠指定的毫秒数，等待后再进行下一次重试
                    Thread.sleep(sleepTime);
                }
                backOffInterval = backOffInterval  * 2;// 指数退避
            }
        }
        // 如果超过最大重试次数后仍未成功，则抛出异常，提示重试次数已超出限制
        throw new Exception("Maximum retry times exceeded, giving up.");
    }
}
