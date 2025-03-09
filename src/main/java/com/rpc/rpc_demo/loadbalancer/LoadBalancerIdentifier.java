package com.rpc.rpc_demo.loadbalancer;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 11:20
 */
public class LoadBalancerIdentifier {
    /**
     * 轮询
     */
    public static String ROUND_ROBIN = "roundRobin";

    /**
     * 随机
     */
    public static String RANDOM = "random";

    /**
     * 一致性哈希
     */
    public static String CONSISTENT_HASH = "consistentHash";

    /**
     * 加权轮询
     */
    public static String WEIGHTED_ROUND_ROBIN = "weightedRoundRobin";
}
