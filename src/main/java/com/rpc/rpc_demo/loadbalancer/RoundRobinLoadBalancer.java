package com.rpc.rpc_demo.loadbalancer;

import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 轮询负载均衡器 （消费端 客户端 使用）
 * @Author veritas
 * @Data 2025/3/9 12:33
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    /**
     * 当前索引
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    @Override
    public ServiceMetaData select(Map<String, Object> requestParams, List<ServiceMetaData> serviceMetaDataList) {
        if (serviceMetaDataList.isEmpty()) {
            return null;
        }
        // 只有一个服务无需轮询
        int size = serviceMetaDataList.size();
        if (size == 1) {
            return serviceMetaDataList.get(0);
        }
        // 得到计数器 并然计数器++
        int increment = currentIndex.getAndIncrement();
        // 取模轮询
        return serviceMetaDataList.get(increment % size);
    }
}
