package com.rpc.rpc_demo.fault.tolerant;


import com.rpc.rpc_demo.spi.SPILoader;
import lombok.extern.slf4j.Slf4j;

/**
 * @author houyunfei
 */
@Slf4j
public class TolerantStrategyFactory {
    static {
        // 本质是 META-INF 有一个名字TolerantStrategy.class为全类名的文件
        // 里面每一个是key=value key是标识符，value是注册中心的实现的全类名
        SPILoader.load(TolerantStrategy.class);
    }


    /**
     * 默认容错策略
     */
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();


    /**
     * 获取实例
     * @param key
     * @return
     */
    public static TolerantStrategy getInstance(String key) {
        return SPILoader.getInstance(TolerantStrategy.class, key);
    }
}
