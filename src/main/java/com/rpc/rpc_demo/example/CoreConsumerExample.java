package com.rpc.rpc_demo.example;

import com.rpc.rpc_demo.bootstrap.ConsumerBootstrap;
import com.rpc.rpc_demo.model.User;
import com.rpc.rpc_demo.v1.service.UserService;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/9 18:51
 */
public class CoreConsumerExample {
/*    public static void main(String[] args) {
        ConsumerBootstrap.init();

        // 获取 代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("cxk");

        User user1 = userService.getUser(user);
        if (user1 != null) {
            System.out.println(user1.getName());
        } else {
            System.out.println("user==null");
        }
    }*/
}
