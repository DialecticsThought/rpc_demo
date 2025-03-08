package com.rpc.rpc_demo.model;
import cn.hutool.core.util.StrUtil;

/**
 * @author jiahao.liu
 * @description
 * @date 2025/03/08 17:28
 */
public class ServiceMetaData {
    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务版本号
     */
    private String serviceVersion = "1.0";

    /**
     * 服务地址
     */
    private String serviceAddress;
    /**
     * 服务的域名
     */
    private String serviceHost;
    /**
     * 服务的端口
     */
    private Integer servicePort;

    /**
     * 服务分组（暂未实现）
     */
    private String serviceGroup = "default";
    /**
     * 服务权重
     */
    private int weight = 1;

    /**
     * 获取服务键名
     * eg:serviceName:serviceVersion
     */
    public String getServiceIdentifier() {
        return String.format("%s:%s", serviceName, serviceVersion);
    }

    /**
     * 获取服务节点键名
     *  eg:serviceName:serviceVersion:serviceHost:servicePort
     */
    public String getServiceNodeIdentifier() {
        return String.format("%s/%s:%s", getServiceIdentifier(), serviceHost,servicePort);
    }

    /**
     * 获取完整的服务地址
     */
    public String getServiceAddress() {
        if (!StrUtil.contains(serviceHost, "http")) {
            return String.format("http://%s:%s", serviceHost, servicePort);
        }
        return String.format("%s:%s", serviceHost, servicePort);
    }
}
