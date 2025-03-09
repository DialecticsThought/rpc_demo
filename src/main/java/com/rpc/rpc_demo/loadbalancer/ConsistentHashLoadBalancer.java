package com.rpc.rpc_demo.loadbalancer;

import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 17:08
 */
public class ConsistentHashLoadBalancer implements LoadBalancer {

    /**
     * 一致性Hash环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaData> virtualNodes = new TreeMap<>();

    /**
     *
     * @param requestParams       请求参数
     * @param serviceMetaDataList 服务列表
     * @return
     */
    @Override
    public ServiceMetaData select(Map<String, Object> requestParams, List<ServiceMetaData> serviceMetaDataList) {
        return null;
    }
}
