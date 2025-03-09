package com.rpc.rpc_demo.fault.tolerant;

/**
 * @Description 降级策略标识符常量
 * @Author veritas
 * @Data 2025/3/9 11:22
 */
public class TolerantStrategyIdentifier {
    /**
     * 快速失败
     */
    public static String FAIL_FAST = "failFast";

    /**
     * 静默处理
     */
    public static String FAIL_SAFE = "failSafe";

    /**
     * 故障恢复
     */
    public static String FAIL_BACK = "failBack";

    /**
     * 故障转移
     */
    public static String FAIL_OVER = "failOver";
}
