package com.rpc.rpc_demo.fault.retry;

import com.rpc.rpc_demo.model.RpcResponse;

import java.util.concurrent.Callable;

/**
 * @Description 该策略 就是不重试
 * @Author veritas
 * @Data 2025/3/9 15:28
 */
public class NoRetryStrategy implements RetryStrategy{

    /**
     *
     * @param callable 重试的方法 代表一个任务
     * @return
     * @throws Exception
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception {
        return callable.call();
    }
}
