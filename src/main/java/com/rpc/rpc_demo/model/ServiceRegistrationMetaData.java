package com.rpc.rpc_demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description 服务提供者用
 * @Author veritas
 * @Data 2025/3/9 12:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceRegistrationMetaData<T> {
    /**
     * 服务名称(本质就是提供服务的抽象类)
     */
    private String serviceName;

    /**
     * 服务实现类
     */
    private Class<? extends T> serviceImplementClass;
}
