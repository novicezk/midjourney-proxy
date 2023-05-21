package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.dto.DescribeDTO;
import com.github.novicezk.midjourney.dto.SubmitDTO;
import com.github.novicezk.midjourney.dto.UVSubmitDTO;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.support.BannedPromptHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import com.github.novicezk.midjourney.util.UVData;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;

@Api(tags = "任务提交")
@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
	private final TranslateService translateService;
	private final TaskStoreService taskStoreService;
	private final ProxyProperties properties;
	private final BannedPromptHelper bannedPromptHelper;
	private final TaskService taskService;

	@ApiOperation(value = "提交Imagine或UV任务")
	@PostMapping("/submit")
	public Message<String> submit(@RequestBody SubmitDTO submitDTO) {
		if (submitDTO.getAction() == null) {
			return Message.validationError();
		}
		if ((submitDTO.getAction() == Action.UPSCALE || submitDTO.getAction() == Action.VARIATION)
				&& (submitDTO.getIndex() < 1 || submitDTO.getIndex() > 4)) {
			return Message.validationError();
		}
		Task task = new Task();
		task.setNotifyHook(CharSequenceUtil.isBlank(submitDTO.getNotifyHook()) ? this.properties.getNotifyHook() : submitDTO.getNotifyHook());
		task.setId(RandomUtil.randomNumbers(16));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(submitDTO.getState());
		task.setAction(submitDTO.getAction());
		if (Action.IMAGINE.equals(submitDTO.getAction())) {
			String prompt = submitDTO.getPrompt();
			if (CharSequenceUtil.isBlank(prompt)) {
				return Message.validationError();
			}
			task.setKey(task.getId());
			task.setPrompt(prompt);
			String promptEn;
			int paramStart = prompt.indexOf(" --");
			if (paramStart > 0) {
				promptEn = this.translateService.translateToEnglish(prompt.substring(0, paramStart)).trim() + prompt.substring(paramStart);
			} else {
				promptEn = this.translateService.translateToEnglish(prompt).trim();
			}
			if (this.bannedPromptHelper.isBanned(promptEn)) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "可能包含敏感词");
			}
			task.setPromptEn(promptEn);
			task.setFinalPrompt("[" + task.getId() + "] " + promptEn);
			task.setDescription("/imagine " + submitDTO.getPrompt());
			return this.taskService.submitImagine(task);
		}
		if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
			return Message.validationError();
		}
		Task targetTask = this.taskStoreService.getTask(submitDTO.getTaskId());
		if (targetTask == null) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务状态错误");
		}
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setFinalPrompt(targetTask.getFinalPrompt());
		task.setRelatedTaskId(ConvertUtils.findTaskIdByFinalPrompt(targetTask.getFinalPrompt()));
		task.setKey(targetTask.getMessageId() + "-" + submitDTO.getAction());
		if (Action.UPSCALE.equals(submitDTO.getAction())) {
			task.setDescription("/up " + submitDTO.getTaskId() + " U" + submitDTO.getIndex());
			return this.taskService.submitUpscale(task, targetTask.getMessageId(), targetTask.getMessageHash(), submitDTO.getIndex());
		} else if (Action.VARIATION.equals(submitDTO.getAction())) {
			task.setDescription("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
			return this.taskService.submitVariation(task, targetTask.getMessageId(), targetTask.getMessageHash(), submitDTO.getIndex());
		} else {
			return Message.of(Message.VALIDATION_ERROR_CODE, "不支持的操作");
		}
	}

	@ApiOperation(value = "提交选中放大或变换任务")
	@PostMapping("/submit-uv")
	public Message<String> submitUV(@RequestBody UVSubmitDTO uvSubmitDTO) {
		UVData uvData = ConvertUtils.convertUVData(uvSubmitDTO.getContent());
		if (uvData == null) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "/up 参数错误");
		}
		SubmitDTO submitDTO = new SubmitDTO();
		submitDTO.setAction(uvData.getAction());
		submitDTO.setTaskId(uvData.getId());
		submitDTO.setIndex(uvData.getIndex());
		submitDTO.setState(uvSubmitDTO.getState());
		submitDTO.setNotifyHook(uvSubmitDTO.getNotifyHook());
		return submit(submitDTO);
	}

	@ApiOperation(value = "提交Describe图生文任务")
	@PostMapping("/describe")
	public Message<String> describe(@RequestBody DescribeDTO describeDTO) {
		if (CharSequenceUtil.isBlank(describeDTO.getBase64())) {
			return Message.validationError();
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(describeDTO.getBase64());
		} catch (MalformedURLException e) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "base64格式错误");
		}
		Task task = new Task();
		task.setSubmitTime(System.currentTimeMillis());
		task.setId(RandomUtil.randomNumbers(16));
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setState(describeDTO.getState());
		task.setAction(Action.DESCRIBE);
		task.setDescription("/describe " + taskFileName);
		task.setKey(taskFileName);
		task.setNotifyHook(CharSequenceUtil.isBlank(describeDTO.getNotifyHook()) ? this.properties.getNotifyHook() : describeDTO.getNotifyHook());
		return this.taskService.submitDescribe(task, dataUrl);
	}

}
