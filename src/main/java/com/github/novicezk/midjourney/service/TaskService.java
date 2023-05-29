package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;
import java.util.stream.Stream;

public interface TaskService {

	Task getRunningTask(String id);

	Stream<Task> findRunningTask(TaskCondition condition);

	SubmitResultVO submitImagine(Task task, DataUrl dataUrl);

	SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index);

	SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index);

	SubmitResultVO submitDescribe(Task task, DataUrl dataUrl);

	SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls);
}