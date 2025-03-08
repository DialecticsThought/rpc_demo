package com.rpc.rpc_demo.serializer.impl;

import com.rpc.rpc_demo.serializer.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 11:15
 */
public class JDKSerializer implements Serializer {
    @Override
    public <T> byte[] serialize(T obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        // 写入对象
        objectOutputStream.writeObject(obj);
        // 关闭 流
        objectOutputStream.close();
        //
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

        try{
            return (T)objectInputStream.readObject();
        }catch(Exception e){
            throw new IOException(e);
        }finally{
            objectInputStream.close();
        }
    }
}
