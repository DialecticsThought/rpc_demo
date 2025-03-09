package com.rpc.rpc_demo.communication.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 12:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistrationMetaData<T>  {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务实现类
     */
    private Class<? extends T> serviceImplementClass;
}
