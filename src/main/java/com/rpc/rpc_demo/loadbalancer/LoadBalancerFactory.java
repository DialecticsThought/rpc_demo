package com.rpc.rpc_demo.loadbalancer;

import com.rpc.rpc_demo.spi.SPILoader;


/**
 * 负载均衡器工厂
 */
public class LoadBalancerFactory {
    static {
        // 本质是 META-INF 有一个名字LoadBalancer.class为全类名的文件
        // 里面每一个是key=value key是标识符，value是注册中心的实现的全类名
        SPILoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RandomLoadBalancer();

    /**
     * 获取负载均衡器
     *
     * @param key 负载均衡器键名
     * @return
     */
    public static LoadBalancer getInstance(String key) {
        return SPILoader.getInstance(LoadBalancer.class, key);
    }
}
