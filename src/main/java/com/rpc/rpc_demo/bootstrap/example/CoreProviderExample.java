package com.rpc.rpc_demo.bootstrap.example;

import com.rpc.rpc_demo.bootstrap.ProviderBootstrap;
import com.rpc.rpc_demo.model.ServiceRegistrationMetaData;
import com.rpc.rpc_demo.v1.service.UserService;
import com.rpc.rpc_demo.v1.service.impl.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 18:52
 */
public class CoreProviderExample {
    public static void main(String[] args) {
        // 这里 服务提供者 只提供 UserServiceImpl服务
        List<ServiceRegistrationMetaData<?>> serviceRegistrationMetaData = new ArrayList<>();

        ServiceRegistrationMetaData registrationMetaData =
                new ServiceRegistrationMetaData(UserService.class.getName(), UserServiceImpl.class);

        serviceRegistrationMetaData.add(registrationMetaData);

        ProviderBootstrap.init(serviceRegistrationMetaData);
    }
}
