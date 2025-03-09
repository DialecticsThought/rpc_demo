package com.rpc.rpc_demo;

import cn.hutool.core.util.IdUtil;
import com.rpc.rpc_demo.constant.RpcConstant;
import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.protocol.*;
import com.rpc.rpc_demo.serializer.Serializer;
import com.rpc.rpc_demo.communication.protocol.*;
import com.rpc.rpc_demo.spi.SPILoader;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class RpcSelfApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testLoad() {
        Map<String, Class<?>> classMap = SPILoader.load(Serializer.class);
        for (Map.Entry<String, Class<?>> entry : classMap.entrySet()) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }
    }

    @Test
    void getInstance() {
        this.testLoad();
        Object jdk = SPILoader.getInstance(Serializer.class, "jdk");
        System.out.println(jdk);
    }

    @org.junit.Test
    public void testEncodeAndDecode() throws Exception {
        // 构造消息
        ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
        ProtocolMessage.Header header = new ProtocolMessage.Header();

        header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
        header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
        header.setSerializer((byte) ProtocolMessageSerializerEnum.JDK.getKey());
        header.setType((byte) ProtocolMessageTypeEnum.REQUEST.getKey());
        header.setStatus((byte) ProtocolMessageStatusEnum.OK.getValue());
        header.setRequestId(IdUtil.getSnowflakeNextId());
        header.setBodyLength(0);

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("myService");
        rpcRequest.setMethodName("myMethod");
        rpcRequest.setServiceVersion(RpcConstant.DEFAULT_SERVICE_VERSION);
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"hello", "world"});
        protocolMessage.setHeader(header);
        protocolMessage.setBody(rpcRequest);

        Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
        ProtocolMessage<?> message = ProtocolMessageDecoder.decode(encodeBuffer);
        System.out.println("message: " + message);
        Assert.assertNotNull(message);
    }
}
