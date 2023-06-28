package com.github.novicezk.midjourney.wss;

import com.neovisionaries.ws.client.ProxySettings;
import com.neovisionaries.ws.client.WebSocketFactory;
import org.apache.logging.log4j.util.Strings;

import java.util.Objects;

public interface WebSocketStarter {

    void start() throws Exception;

    default void initProxy(String host, Integer port) {
        if (Strings.isNotBlank(host) && Objects.nonNull(port)) {
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", String.valueOf(port));
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", String.valueOf(port));
        }
    }

    default WebSocketFactory createWebSocketFactory(String host, Integer port) {
        WebSocketFactory webSocketFactory = new WebSocketFactory().setConnectionTimeout(10000);
        if (Strings.isNotBlank(host) && Objects.nonNull(port)) {
            ProxySettings proxySettings = webSocketFactory.getProxySettings();
            proxySettings.setHost(host);
            proxySettings.setPort(port);
        }
        return webSocketFactory;
    }
}
