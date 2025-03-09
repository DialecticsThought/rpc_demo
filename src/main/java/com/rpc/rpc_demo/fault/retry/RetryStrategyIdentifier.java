package com.rpc.rpc_demo.fault.retry;

/**
 * @Description 重试策略标识符常量
 * @Author veritas
 * @Data 2025/3/9 11:23
 */
public class RetryStrategyIdentifier {
    /**
     * 不重试
     */
    public static final String NO = "no";

    /**
     * 固定间隔重试
     */
    public static final String FIXED_INTERVAL = "fixedInterval";

    /**
     * 线性退避重试
     */
    public static final String LINEAR = "linear";

    /**
     * 指数退避重试
     */
    public static final String EXPONENTIAL_BACKOFF = "exponentialBackoff";
}
