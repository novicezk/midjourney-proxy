package com.github.novicezk.midjourney.loadbalancer;


import com.github.novicezk.midjourney.domain.DiscordAccount;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.support.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface DiscordInstance extends DiscordService {

	String getInstanceId();

	DiscordAccount account();

	boolean isAlive();

	void startWss() throws Exception;

	List<Task> getRunningTasks();

	void exitTask(Task task);

	Map<String, Future<?>> getRunningFutures();

	SubmitResultVO submitTask(Task task, Callable<Message<Void>> discordSubmit);

}
