package com.rpc.rpc_demo.v1;

import com.rpc.rpc_demo.v1.proxy.UserServiceStaticProxy;

/**
 * @author jiahao.liu
 * @description
 * 服务消费者 会向 服务提供者 发送请求，请求 包含 要执行的 方法 和 方法对应的参数
 * 发送请求的本质是利用静态代理(这一个版本)，在静态代理的方法中实现 向 服务提供者 发送请求
 * @date 2025/03/08 16:22
 */
public class EasyConsumerExample {
    public static void main(String[] args) {
        UserServiceStaticProxy userServiceProxy = new UserServiceStaticProxy();
        userServiceProxy.getNumber();
    }
}
