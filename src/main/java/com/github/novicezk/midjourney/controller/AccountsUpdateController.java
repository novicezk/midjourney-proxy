package com.github.novicezk.midjourney.controller;

import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.dto.AccountDTO;
import com.github.novicezk.midjourney.support.DiscordAccountInitializer;
import com.github.novicezk.midjourney.util.AccountsUpdateUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "动态更改账号")
@RestController
@Slf4j
@RequestMapping("/update")
@RequiredArgsConstructor
public class AccountsUpdateController {
    @Autowired
    private AccountsUpdateUtils accountsUpdateUtils;

    @Autowired
    private ProxyProperties properties;

    @ApiOperation(value = "修改账号")
    @PostMapping ("/accounts")
    public String updateAccounts(@RequestBody List<AccountDTO.Account> list) {
        //修改本地的yml的accounts
//      accountsUpdateUtils.updateConfig(list);
      //发送新的accounts到nacos
        accountsUpdateUtils.sendAccountsTONacos(list);
      log.info("账号更新为："+list.toString());
      return "账号更新成功";
    }

    @ApiOperation(value = "获取账号")
    @GetMapping ("/getInfo")
    public String getInfo() {
        return properties.getAccounts().toString() ;
    }

    @ApiOperation(value = "删除账号")
    @GetMapping("/delete")
    public String deleteByGuiId(@RequestParam String guiId) throws JsonProcessingException, NacosException {
        accountsUpdateUtils.deleteByGuiId(guiId);
        return "删除账号:"+guiId;
    }

    @ApiOperation(value = "添加账号")
    @PostMapping("/add")
    public String addAccount(@RequestBody AccountDTO.Account account) throws JsonProcessingException, NacosException {
        accountsUpdateUtils.addAccount(account);
        return "添加账号成功:"+account.getGuildId();
    }
}
