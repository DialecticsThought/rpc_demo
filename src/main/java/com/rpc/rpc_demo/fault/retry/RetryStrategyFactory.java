package com.rpc.rpc_demo.fault.retry;

import com.rpc.rpc_demo.spi.SPILoader;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 16:46
 */
@Slf4j
public class RetryStrategyFactory {
    static {
        // 本质是 META-INF 有一个名字RetryStrategy.class为全类名的文件
        // 里面每一个是key=value key是标识符，value是注册中心的实现的全类名
        SPILoader.load(RetryStrategy.class);
    }

    /**
     * 默认重试策略
     */
    private static final RetryStrategy DEFAULT_RETRY_STRATEGY = new NoRetryStrategy();

    /**
     * 获取重试策略实例
     * @param key
     * @return
     */
    public static RetryStrategy getInstance(String key) {
        return SPILoader.getInstance(RetryStrategy.class, key);
    }
}
