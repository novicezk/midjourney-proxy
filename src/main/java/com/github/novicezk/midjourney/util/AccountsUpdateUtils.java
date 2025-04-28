package com.github.novicezk.midjourney.util;


import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.github.novicezk.midjourney.dto.AccountDTO;
import com.github.novicezk.midjourney.service.NacosConfigManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AccountsUpdateUtils {
    @Value("${filePath}")
    private  String filePath;
    @Value("midjourney-proxy-dev.yaml")
    private  String dataId;
    @Value("DEFAULT_GROUP")
    private String group;
    @Value("yaml")
    private String type;
    public void updateConfig(List<AccountDTO.Account> list) {
        Yaml yaml = new Yaml();
        Map<String, Object> obj;

        try (InputStream inputStream = new FileInputStream(filePath)) {
            obj = yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 获取 mj.accounts 配置
        List<Map<String, Object>> accounts = (List<Map<String, Object>>) ((Map<String, Object>) obj.get("mj")).get("accounts");
        if (accounts == null) {
            accounts = new ArrayList<>();
            ((Map<String, Object>) obj.get("mj")).put("accounts", accounts);
        }
        //清空当前列表
        accounts.clear();
        for (AccountDTO.Account accountDTO : list) {
            Map<String, Object> account = new LinkedHashMap<>();
            account.put("guild-id", accountDTO.getGuildId());
            account.put("channel-id", accountDTO.getChannelId());
            account.put("user-token", accountDTO.getUserToken());
            account.put("user-agent", accountDTO.getUserAgent());
            account.put("core-size", accountDTO.getCoreSize());
            account.put("queue-size", accountDTO.getQueueSize());
            accounts.add(account);
        }

        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Yaml newYaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath)) {
            newYaml.dump(obj, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  void sendAccountsTONacos(List<AccountDTO.Account> list) {
        try {

            NacosConfigManager nacosConfigManager =new NacosConfigManager();
            // 构造顶层对象
            Map<String, Object> root = new HashMap<>();
            Map<String, Object> mjMap = new HashMap<>();
            mjMap.put("accounts", list);
            root.put("mj", mjMap);

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            String yamlContent = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

            nacosConfigManager.updateConfig(dataId, group, yamlContent,type);

        } catch (NacosException e) {
            e.printStackTrace();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }
    public void deleteByGuiId(String guiId) throws NacosException, JsonProcessingException {
        NacosConfigManager nacosConfigManager = new NacosConfigManager();
        String content = nacosConfigManager.getConfig(dataId, group, 5000);
        ObjectMapper mapper = new YAMLMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AccountDTO accountDTO = mapper.readValue(content, AccountDTO.class);
        accountDTO.getMj().setAccounts(accountDTO.getMj().getAccounts().stream()
                .filter(account -> !account.getGuildId().equals(guiId))
                .collect(Collectors.toList()));
        sendAccountsTONacos(accountDTO.getMj().getAccounts());
    }
    public void addAccount(AccountDTO.Account account) throws NacosException, JsonProcessingException {
        NacosConfigManager nacosConfigManager = new NacosConfigManager();
        String content = nacosConfigManager.getConfig(dataId, group, 5000);
        ObjectMapper mapper = new YAMLMapper();
        //忽略未知属性
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AccountDTO accountDTO;
        if(content==null){
            accountDTO =new AccountDTO();
            accountDTO.getMj().setAccounts(new ArrayList<>());
        }else {
             accountDTO = mapper.readValue(content, AccountDTO.class);
        }
        if(accountDTO.getMj().getAccounts()==null){
            accountDTO.getMj().setAccounts(new ArrayList<>() );
        }
        accountDTO.getMj().getAccounts().add(account);
        sendAccountsTONacos(accountDTO.getMj().getAccounts());
    }
}
