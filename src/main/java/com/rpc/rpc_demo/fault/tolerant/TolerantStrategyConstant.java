package com.rpc.rpc_demo.fault.tolerant;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 16:41
 */
public class TolerantStrategyConstant {
    /**
     * 服务列表 用于容错策略
     */
    public static String SERVICE_LIST = "serviceList";

    /**
     * 当前正在调用的服务
     */
    public static String CURRENT_SERVICE = "currentService";

    /**
     * RPC Request
     */
    public static String RPC_REQUEST = "rpcRequest";
}
