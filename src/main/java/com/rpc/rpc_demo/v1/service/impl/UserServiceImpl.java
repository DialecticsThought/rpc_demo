package com.rpc.rpc_demo.v1.service.impl;


import com.rpc.rpc_demo.model.User;
import com.rpc.rpc_demo.v1.service.UserService;

/**
 * @author jiahao.liu
 * @description
 * TODO 这个方法 是给 服务提供者用的
 * @date 2025/03/08 12:47
 */
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        System.out.println("UserServiceImpl.getUser username=" + user.getName());
        return user;
    }
}
