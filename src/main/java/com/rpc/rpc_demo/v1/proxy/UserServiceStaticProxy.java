package com.rpc.rpc_demo.v1.proxy;


import com.rpc.rpc_demo.model.RpcRequest;
import com.rpc.rpc_demo.model.RpcResponse;
import com.rpc.rpc_demo.model.User;
import com.rpc.rpc_demo.serializer.impl.JDKSerializer;
import com.rpc.rpc_demo.v1.service.UserService;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

/**
 * @author jiahao.liu
 * @description
 * TODO 静态代理： 本质在实现方法中，向服务提供者 发送请求
 * @date 2025/03/08 11:05
 */
public class UserServiceStaticProxy implements UserService {
    @Override
    public User getUser(User user) {
        // 这里指定 序列化方式
        JDKSerializer jdkSerializer = new JDKSerializer();

        // 发送请求
        // 意思是 让服务生产者执行UserService.getUser()方法
        // 该方法的参数是服务消费者提供的user对象
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();

        try{
            byte[] bodyBytes = jdkSerializer.serialize(rpcRequest);
            byte[] result;
            String url = "http://localhost:8080";
            try(HttpResponse httpResponse = HttpRequest.post(url).body(bodyBytes).execute()){
                result = httpResponse.bodyBytes();
            }
            RpcResponse response = jdkSerializer.deserialize(result, RpcResponse.class);

            return (User) response.getData();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
