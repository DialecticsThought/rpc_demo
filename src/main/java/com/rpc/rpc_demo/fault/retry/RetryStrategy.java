package com.rpc.rpc_demo.fault.retry;

import com.rpc.rpc_demo.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 13:14
 */
public interface RetryStrategy {
    /**
     * 该方法接受一个 Callable 对象作为参数,表示需要重试的方法调用。
     * <p>
     * 方法实现需要根据具体的重试策略,决定是否需要重试,并执行重试操作。
     * <p>
     * 如果重试成功,则返回调用结果 RpcResponse。如果重试失败,则抛出异常
     *
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
