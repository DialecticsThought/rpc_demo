package com.rpc.module1.service;

import com.rpc.module1.model.User;

/**
 * @Description
 * @Author veritas
 * @Data 2025/3/8 10:32
 */
public interface UserService {
    /**
     * 获取用户
     *
     * @param user
     * @return
     */
    User getUser(User user);


    /**
     * 获取数字
     */
    default short getNumber() {
        return 1;
    }

}
