package com.rpc.rpc_demo.fault.tolerant;

import com.rpc.rpc_demo.model.RpcResponse;

import java.util.Map;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 13:11
 */
public interface TolerantStrategy {
    /**
     * 这个方法定义了容错处理的具体实现。
     * 它接收上下文信息和异常对象作为参数,并返回一个 RpcResponse 作为处理结果。
     * 容错策略的实现者需要根据具体的业务需求和故障情况,编写相应的容错逻辑,并返回一个合适的响应结果。
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
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}
