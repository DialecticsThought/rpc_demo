package com.rpc.rpc_demo.springboot.starter.bootstrap;

import com.rpc.rpc_demo.proxy.ServiceProxyFactory;
import com.rpc.rpc_demo.springboot.starter.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;

/**
 * @Description
 * 主要作用是在 Spring 容器初始化 Bean 实例后,检查这些 Bean 中是否有被 @RpcReference 注解标注的字段,如果有,则为这些字段生成代理对象并注入
 * @Author veritas
 * @Data 2025/3/9 21:27
 */
@Slf4j
public class RpcConsumerBootstrap implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        // 遍历对象的所有属性
        Field[] declaredFields = beanClass.getDeclaredFields();
        for (Field field : declaredFields) {
            // 如果字段被 @RpcReference 注解标注,则获取该注解实例
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                // 找到 注解的  interfaceClass属性的属性值是什么 这个属性值是一个提供服务的类
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }
                log.info("生成代理对象: {} : {}", interfaceClass.getName(),field.getType());
                field.setAccessible(true);
                log.info("生成代理对象:{}", interfaceClass.getName());
                // 为属性生成代理对象
                Object proxy = ServiceProxyFactory.getProxy(interfaceClass);
                try {
                    field.set(bean, proxy);
                    field.setAccessible(false);
                } catch (IllegalAccessException e) {
                    System.out.println("生成代理对象失败");
                    throw new RuntimeException(e);
                }
            }

        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }
}
