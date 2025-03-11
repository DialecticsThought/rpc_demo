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
 * @Description 这种策略和故障恢复差不多，都是尝试其他服务，
 * 只不过这个在故障服务恢复正常后触发,目的是将流量切换回原来的服务实例
 * @Author veritas
 * @Data 2025/3/9 16:55
 */
@Slf4j
public class FailBackTolerantStrategy implements TolerantStrategy {
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
