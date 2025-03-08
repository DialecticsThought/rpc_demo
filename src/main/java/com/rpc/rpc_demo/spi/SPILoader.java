package com.rpc.rpc_demo.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import com.rpc.rpc_demo.serializer.Serializer;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jiahao.liu
 * @description SPI加载器 （支持键值对映射）
 * TODO SPI的每一个文件名字 都是 需要加载的类在项目的类路径
 * @date 2025/03/08 16:30
 */
@Log4j2
public class SPILoader {
    /**
     * 系统 SPI 目录
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";
    /**
     * 用户自定义SPI目录
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{
            RPC_SYSTEM_SPI_DIR,
            RPC_CUSTOM_SPI_DIR,
    };

    /**
     * 存储已加载的类：<接口名,<key,接口实现类 > >
     */
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();
    /**
     * 对象实例缓存 避免重放  <类路径,对象实例 >，单例模式
     */
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    /**
     * 动态加载的类列表 这里就是把序列化器的类放入该集合
     * 这里集合只有Serializer.class 说明 只加载Serializer的实现类
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     */
    public static void loadAll() {
        log.info("加载所有的SPI");
        for (Class<?> aClass : LOAD_CLASS_LIST) {
            load(aClass);
        }
    }

    /**
     * 加载某个类型
     * 本质就是加载文字为 该类型类路径的文件
     * @param loadClass
     * @return
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为:{} 的SPI", loadClass.getName());
        // 扫描路径，用户自定义的SPI 优先级 高于 系统SPI
        Map<String, Class<?>> keyClassMap = new HashMap<>();
        // SPI的每一个文件名字 都是 需要加载的类在项目的类路径
        // 根据文件名字找到 该文件 ，把文件里的key=value 都得到
        for (String scanDir : SCAN_DIRS) {
            log.info("扫描路径为 {}", scanDir + loadClass.getName());
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            // 读取每一个资源的文件
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    // eg: jdk=com.yunfei.rpc.serializer.JdkSerializer
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length != 2) {
                            log.error("SPI配置文件格式错误");
                            continue;
                        }
                        String key = split[0];
                        String className = split[1];
                        log.info("加载 {} SPI配置文件 key={} className={}",
                                scanDir.equals(RPC_CUSTOM_SPI_DIR) ? "自定义" : "系统", key, className);
                        keyClassMap.put(key, Class.forName(className));
                    }
                } catch (Exception e) {
                    log.error("加载SPI配置文件失败", e);
                }
            }
        }
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    /**
     * 获取某个类型的实例
     *
     * @param tClass
     * @param key
     * @param <T>    类型
     */
    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);
        if (keyClassMap == null) {
            throw new RuntimeException(String.format("SpiLoader 未加载%s 类型", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s不存在 key= %s", tClassName, key));
        }
        // 获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        // 从实例缓存中加载指定类型的实例
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                String format = String.format("实例化 %s 失败", implClassName);
                throw new RuntimeException(format, e);
            }
        }
        return (T) instanceCache.get(implClassName);
    }
}
