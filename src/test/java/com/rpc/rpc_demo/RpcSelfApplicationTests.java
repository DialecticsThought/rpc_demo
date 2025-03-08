package com.rpc.rpc_demo;

import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.spi.SPILoader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class RpcSelfApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void load() {
        Map<String, Class<?>> classMap = SPILoader.load(Serializer.class);
        for (Map.Entry<String, Class<?>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    @Test
    void getInstance() {
        this.load();
        Object jdk = SPILoader.getInstance(Serializer.class, "jdk");
        System.out.println(jdk);
    }

}
