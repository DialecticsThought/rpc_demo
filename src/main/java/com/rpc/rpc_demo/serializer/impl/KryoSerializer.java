package com.rpc.rpc_demo.serializer.impl;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.rpc.rpc_demo.serializer.Serializer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 12:00
 */
public class KryoSerializer implements Serializer {

    // Kryo 对象不是线程安全的，因此使用 ThreadLocal 来保证线程安全
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 设置动态序列化和反序列化类，不能提前注册类（可能有安全问题）
        kryo.setRegistrationRequired(false);

        return kryo;
    });

    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        KRYO_THREAD_LOCAL.get().writeObject(output, obj);
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);

        T res = KRYO_THREAD_LOCAL.get().readObject(input, clazz);

        input.close();

        return res;
    }
}
