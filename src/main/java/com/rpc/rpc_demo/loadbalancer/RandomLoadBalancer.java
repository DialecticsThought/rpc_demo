package com.rpc.rpc_demo.loadbalancer;

import com.rpc.rpc_demo.model.ServiceMetaData;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @Description 随机负载均衡器 （消费端 客户端 使用）
 * @Author veritas
 * @Data 2025/3/9 12:30
 */
public class RandomLoadBalancer implements LoadBalancer {
    private final Random random = new Random();

    @Override
    public ServiceMetaData select(Map<String, Object> requestParams, List<ServiceMetaData> serviceMetaDataList) {
        // 得到服务提供者的实例
        int size = serviceMetaDataList.size();

        if (size == 0) {// 没有
            return null;
        }
        if (size == 1) {// 只有一个
            return serviceMetaDataList.get(0);
        }
        return serviceMetaDataList.get(random.nextInt(size));
    }
}
