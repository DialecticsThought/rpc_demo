package com.rpc.rpc_demo.fault.tolerant;

import com.rpc.rpc_demo.RpcContext;
import com.rpc.rpc_demo.communication.server.VertxTcpClient;
import com.rpc.rpc_demo.fault.retry.RetryStrategy;
import com.rpc.rpc_demo.fault.retry.RetryStrategyFactory;
import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.model.ServiceMetaData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @Description 故障恢复策略
 * @Author veritas
 * @Data 2025/3/9 16:19
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {
    /**
     * 获取所有的服务列表，只要不是当前的服务，都可以重试一次，如果都失败，那就直接抛异常，不重试了。
     *
     * @param context 上下文，用于传递数据
     *                这个参数是一个上下文对象,用于在容错处理过程中传递一些数据。
     *                在分布式系统中,当一个远程调用出现异常时,我们需要根据当前的上下文信息来决定如何进行容错处理。
     *                这个上下文可以包含一些关键信息,例如:
     *                当前请求的参数
     *                调用链路信息
     *                服务实例的元数据
     *                重试次数等
     *                通过这个上下文对象,容错策略实现可以获取到更丰富的信息,从而做出更加合理的容错决策
     * @param e       异常
     *                这个参数表示在执行远程调用时出现的异常。
     *                容错策略需要根据异常的类型、错误信息等,来决定采取什么样的容错措施。
     *                例如,对于网络异常可以选择重试,而对于业务异常可能需要降级或返回默认响应。
     *                通过分析异常信息,容错策略可以更有针对性地进行容错处理。
     * @return
     */
    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 服务列表 用于容错策略
        List<ServiceMetaData> serviceMetaDataList = (List<ServiceMetaData>) context.get(TolerantStrategyConstant.SERVICE_LIST);
        // 当前正在调用的服务
        ServiceMetaData currentServiceMetaData = (ServiceMetaData) context.get(TolerantStrategyConstant.CURRENT_SERVICE);
        // rpc请求
        RpcRequest rpcRequest = (RpcRequest) context.get(TolerantStrategyConstant.RPC_REQUEST);

        if (serviceMetaDataList == null || serviceMetaDataList.isEmpty()) {
            log.error("FailOverTolerantStrategy doTolerant metaInfos is empty");
            return null;
        }
        // 重试 serviceMetaDataList之外的其他服务
        for (ServiceMetaData serviceMetaData : serviceMetaDataList) {
            if (serviceMetaData.equals(currentServiceMetaData)) {
                continue;
            }
            // 得到重试策略
            // TODO 通过重试策略 对当前的服务实例 发请求
            RetryStrategy retryStrategy = RetryStrategyFactory.getInstance(RpcContext.getRpcConfig().getRetryStrategy());
            try {
                return retryStrategy.doRetry(() -> {
                    return VertxTcpClient.doRequest(rpcRequest, serviceMetaData);
                });
            } catch (Exception exception) {
                // 如果重试再失败，继续重试下一个
                log.error("FailOverTolerantStrategy doTolerant retry fail");
            }
        }
        // 所有服务都重试失败
        throw new RuntimeException("FailOverTolerantStrategy doTolerant all retry fail");
    }
}
