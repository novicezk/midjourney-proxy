package com.github.novicezk.midjourney.service;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;


public class NacosConfigManager {
    private ConfigService configService;

    public NacosConfigManager() throws NacosException {
        configService = ConfigFactory.createConfigService("localhost:8848");
    }

    public void updateConfig(String dataId, String group, String content,String type) throws NacosException {
//        configService.publishConfig(dataId, group, content);
        try {
            boolean isPublishOk = configService.publishConfig(dataId, group, content,type);
            System.out.println("配置发布结果：" + isPublishOk);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

    public String getConfig(String dateId,String group,long time){
        try {
            String content = configService.getConfig(dateId,group,time);
            return content;
        }catch (NacosException e){
            e.printStackTrace();
        }
        return "获取信息失败";
    }
}