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
     * 定义一个TreeMap作为哈希环，用来存放虚拟节点
     * TreeMap会根据key（这里是虚拟节点的哈希值）自动排序，从而形成一个有序的“环”
     */
    private final TreeMap<Integer, ServiceMetaData> virtualNodes = new TreeMap<>();

    // 定义每个真实服务在哈希环上所对应的虚拟节点数量，常量设置为100
    private static final int VIRTUAL_NODE_NUM = 100;

    /**
     * Hash算法实现
     * 根据传入的key对象计算哈希值
     *
     * @param key 用于计算哈希的对象，可以是字符串、请求参数等
     * @return 返回key的hashCode值作为哈希结果
     */
    private int getHash(Object key) {
        return key.hashCode();
    }

    /**
     * 根据请求参数和服务列表选择合适的服务
     *
     * @param requestParams       请求参数，用于计算请求的哈希值
     * @param serviceMetaDataList 服务列表，包含所有可用的服务实例
     * @return 返回选中的服务实例
     */
    @Override
    public ServiceMetaData select(Map<String, Object> requestParams, List<ServiceMetaData> serviceMetaDataList) {
        // 如果服务列表为空，则直接返回null
        if (serviceMetaDataList.isEmpty()) {
            return null;
        }

        // 每次调用select时都重新构建虚拟节点环
        // 这样可以及时反映服务列表的变化（例如新增或下线的服务）
        // 遍历每个真实的服务实例
        for (ServiceMetaData serviceMetaData : serviceMetaDataList) {
            // 对每个服务实例，创建多个虚拟节点，数量由VIRTUAL_NODE_NUM决定
            for (int i = 0; i < VIRTUAL_NODE_NUM; i++) {
                // 拼接服务地址和虚拟节点的编号，形成唯一的虚拟节点标识，如 "192.168.1.1:8080#0"
                int hash = getHash(serviceMetaData.getServiceAddress() + "#" + i);
                // 将计算出的hash值作为key，真实服务实例作为value存入TreeMap中
                virtualNodes.put(hash, serviceMetaData);
            }
        }
        // 根据请求参数计算请求对应的哈希值
        int hash = getHash(requestParams);
        // 从虚拟节点环中找到第一个key大于或等于请求哈希值的节点
        Map.Entry<Integer, ServiceMetaData> entry = virtualNodes.ceilingEntry(hash);
        // 如果没有找到（例如请求的哈希值超出了所有虚拟节点的值），则取环的第一个节点（模拟环形结构）
        if (entry == null) {
            entry = virtualNodes.firstEntry();
        }
        // 返回选中的服务实例
        return entry.getValue();
    }
}
