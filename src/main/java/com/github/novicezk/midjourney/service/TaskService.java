package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import eu.maxschuster.dataurl.DataUrl;

import java.util.stream.Stream;

public interface TaskService {

	Task getRunningTask(String id);

	Stream<Task> findRunningTask(TaskCondition condition);

	Message<String> submitImagine(Task task);

	Message<String> submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitVariation(Task task, String targetMessageId, String targetMessageHash, int index);

	Message<String> submitDescribe(Task task, DataUrl dataUrl);
}