package com.github.novicezk.midjourney.wss.handle;


import com.github.novicezk.midjourney.enums.MessageType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.data.DataObject;

/**
 * todo reroll 消息处理.
 * 原图reroll时，跟imagine相同;
 * 变换后的图reroll
 * 开始(create): **[4619231091196848] cat** - <@1012983546824114217> (Waiting to start)
 * 进度(update): **[4619231091196848] cat** - Variations by <@1012983546824114217> (0%) (relaxed)
 * 完成(create): **[4619231091196848] cat** - Variations by <@1012983546824114217> (relaxed)
 */
public class RerollMessageHandler extends MessageHandler {

	@Override
	public void handle(MessageType messageType, DataObject message) {

	}

	@Override
	public void handle(MessageType messageType, Message message) {

	}
}