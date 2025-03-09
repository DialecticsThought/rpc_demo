package com.rpc.rpc_demo.fault.retry;

import com.github.rholder.retry.*;
import com.rpc.rpc_demo.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * @Description 固定重试间隔策略
 * @Author veritas
 * @Data 2025/3/9 15:30
 */
@Slf4j
public class FixedIntervalRetryStrategy implements RetryStrategy {
    /**
     * TODO 这里用到了guava的retryer
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                // 遇到任何异常类型都进行重试
                .retryIfExceptionOfType(Exception.class)
                // 每次重试之间固定间隔 3 秒
                .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
                // 最多重试 3 次,超过则停止重试
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                // 添加了一个重试监听器,在每次重试时打印当前重试次数
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("重试第 {} 次", attempt.getAttemptNumber());
                    }
                }).build();
        // 最后调用 retryer.call(callable) 执行重试操作,并返回最终的调用结果 RpcResponse
        return retryer.call(callable);
    }
}
