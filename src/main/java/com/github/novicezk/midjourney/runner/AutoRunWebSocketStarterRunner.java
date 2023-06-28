package com.github.novicezk.midjourney.runner;

import com.github.novicezk.midjourney.wss.WebSocketStarter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author NpcZZZZZZ
 * @version 1.0
 * @email 946123601@qq.com
 * @date 2023/6/28
 **/
@Component
@RequiredArgsConstructor
public class AutoRunWebSocketStarterRunner implements ApplicationRunner {
    private final Map<String, WebSocketStarter> webSocketStarterMap;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for (Map.Entry<String, WebSocketStarter> entry : webSocketStarterMap.entrySet()) {
            entry.getValue().start();
        }
    }
}
