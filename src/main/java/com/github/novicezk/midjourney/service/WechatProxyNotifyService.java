package com.github.novicezk.midjourney.service;


public interface WechatProxyNotifyService {

	boolean notifyCreated(String room, String user, String prompt, String messageId);

	boolean notifyImagine(String room, String user, String prompt, String messageId, String imageUrl);

	boolean notifyUp(String room, String user, String imageUrl);

}
