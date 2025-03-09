package com.rpc.rpc_demo.fault.tolerant;

import com.rpc.rpc_demo.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @Description <pre>静默处理策略
 * 特点是:
 * 不通知调用方失败:
 *      当服务调用出现异常时,不会抛出异常,也不会返回错误响应。
 *      而是直接返回一个默认的响应结果。
 * 只记录日志:
 *      异常信息仅仅通过日志的形式记录下来,方便事后排查问题。
 *      但不会将异常信息直接返回给调用方。
 * 适用场景:
 *      这种策略适用于对最终结果不太敏感的场景,比如日志记录、缓存预热等。
 *      即使服务调用失败,也不会影响业务的核心逻辑。
 * 优缺点:
 *      优点是简单易实现,对系统负载影响小。
 *      缺点是可能会丢失一些有价值的业务信息,无法保证最终一致性。
 * </pre>
 * @Author veritas
 * @Data 2025/3/9 16:14
 */
@Slf4j
public class FailSilentTolerantStrategy implements TolerantStrategy {
    // 静默处理 本质 就是返回默认响应
    private final RpcResponse defaultResponse;

    public FailSilentTolerantStrategy() {
        this.defaultResponse = new RpcResponse();
    }

    public FailSilentTolerantStrategy(RpcResponse rpcResponse) {
        this.defaultResponse = rpcResponse;
    }


    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("FailSafeTolerantStrategy doTolerant", e);
        return defaultResponse;
    }
}
