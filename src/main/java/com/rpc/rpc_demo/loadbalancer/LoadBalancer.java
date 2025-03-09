package com.rpc.rpc_demo.loadbalancer;

import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;
import java.util.Map;

/**
 * @Description 负载均衡器 （消费端 客户端 使用）
 * @Author veritas
 * @Data 2025/3/9 12:23
 */
public interface LoadBalancer {
    /**
     * 选择服务调用
     *
     * @param requestParams       请求参数
     * @param serviceMetaDataList 服务列表
     * @return
     */
    ServiceMetaData select(Map<String, Object> requestParams, List<ServiceMetaData> serviceMetaDataList);
}
