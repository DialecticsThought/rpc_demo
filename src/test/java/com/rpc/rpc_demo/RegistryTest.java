package com.rpc.rpc_demo;

import com.rpc.rpc_demo.config.RegistryConfig;
import com.rpc.rpc_demo.model.ServiceMetaData;
import com.rpc.rpc_demo.registry.Registry;
import com.rpc.rpc_demo.registry.impl.EtcdRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 22:04
 */
public class RegistryTest {
    final Registry registry = new EtcdRegistry();

    @Before
    public void init() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("http://localhost:2379");
        registry.init(registryConfig);
    }

    @Test
    public void register() throws Exception {
        ServiceMetaData ServiceMetaData = new ServiceMetaData();
        ServiceMetaData.setServiceName("myService");
        ServiceMetaData.setServiceVersion("1.0");
        ServiceMetaData.setServiceHost("localhost");
        ServiceMetaData.setServicePort(1234);
        registry.register(ServiceMetaData);


        ServiceMetaData = new ServiceMetaData();
        ServiceMetaData.setServiceName("myService");
        ServiceMetaData.setServiceVersion("1.0");
        ServiceMetaData.setServiceHost("localhost");
        ServiceMetaData.setServicePort(1235);
        registry.register(ServiceMetaData);


        ServiceMetaData = new ServiceMetaData();
        ServiceMetaData.setServiceName("myService");
        ServiceMetaData.setServiceVersion("2.0");
        ServiceMetaData.setServiceHost("localhost");
        ServiceMetaData.setServicePort(1234);
        registry.register(ServiceMetaData);
    }

    @Test
    public void unRegister() {
        ServiceMetaData ServiceMetaData = new ServiceMetaData();
        ServiceMetaData.setServiceName("myService");
        ServiceMetaData.setServiceVersion("1.0");
        ServiceMetaData.setServiceHost("localhost");
        ServiceMetaData.setServicePort(1234);
        registry.unRegister(ServiceMetaData);
    }

    @Test
    public void serviceDiscovery() {
        ServiceMetaData ServiceMetaData = new ServiceMetaData();
        ServiceMetaData.setServiceName("myService");
        ServiceMetaData.setServiceVersion("1.0");
        String serviceKey = ServiceMetaData.getServiceIdentifier();
        List<ServiceMetaData> ServiceMetaDataList = registry.serviceDiscovery(serviceKey);
        Assert.assertNotNull(ServiceMetaDataList);
    }

    @Test
    public void heartBeat() throws Exception {
        register();
        Thread.sleep(60 * 1000L);
    }

}

