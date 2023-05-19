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
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.service.TaskService;
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

@Api(tags = "出图模块")
@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
	private final DiscordService discordService;
	private final TranslateService translateService;
	private final TaskService taskService;
	private final ProxyProperties properties;
	private final BannedPromptHelper bannedPromptHelper;

	@ApiOperation(value = "提交任务")
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
		Message<Void> result;
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
			this.taskService.putTask(task.getId(), task);
			result = this.discordService.imagine(task.getFinalPrompt());
		} else {
			if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
				return Message.validationError();
			}
			Task targetTask = this.taskService.getTask(submitDTO.getTaskId());
			if (targetTask == null) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "任务不存在或已失效");
			}
			if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务状态错误：" + targetTask.getStatus());
			}
			task.setPrompt(targetTask.getPrompt());
			task.setPromptEn(targetTask.getPromptEn());
			task.setFinalPrompt(targetTask.getFinalPrompt());
			task.setRelatedTaskId(ConvertUtils.findTaskIdByFinalPrompt(targetTask.getFinalPrompt()));
			String key = targetTask.getMessageId() + "-" + submitDTO.getAction() + "-" +submitDTO.getIndex();
			if (submitDTO.getAction() == Action.UPSCALE && taskService.getTask(key) != null) {
				var uTask  = taskService.getTask(key);
				if (uTask != null) {
					return Message.of(Message.SUCCESS_CODE, "UPSCALE任务已存在", uTask.getId());
				}
			}
			task.setKey(key);
			if (Action.UPSCALE.equals(submitDTO.getAction())) {
				task.setDescription("/up " + submitDTO.getTaskId() + " U" + submitDTO.getIndex());
				this.taskService.putTask(key, task);
				result = this.discordService.upscale(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
			} else if (Action.VARIATION.equals(submitDTO.getAction())) {
				task.setDescription("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
				this.taskService.putTask(key, task);
				result = this.discordService.variation(targetTask.getMessageId(), submitDTO.getIndex(), targetTask.getMessageHash());
			} else {
				return Message.of(Message.VALIDATION_ERROR_CODE, "不支持的操作");
			}
		}
		if (result.getCode() != Message.SUCCESS_CODE) {
			this.taskService.removeTask(task.getId());
			return Message.of(result.getCode(), result.getDescription());
		}
		return Message.success(task.getId());
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

	@ApiOperation(value = "提交describe任务")
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
		task.setId(RandomUtil.randomNumbers(16));
		task.setSubmitTime(System.currentTimeMillis());
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
		if (uploadResult.getCode() != Message.SUCCESS_CODE) {
			return uploadResult;
		}
		String finalFileName = uploadResult.getResult();
		task.setState(describeDTO.getState());
		task.setAction(Action.DESCRIBE);
		task.setDescription("/describe " + taskFileName);
		task.setKey(taskFileName);
		task.setNotifyHook(CharSequenceUtil.isBlank(describeDTO.getNotifyHook()) ? this.properties.getNotifyHook() : describeDTO.getNotifyHook());
		this.taskService.putTask(task.getId(), task);
		Message<Void> message = this.discordService.describe(finalFileName);
		if (message.getCode() != Message.SUCCESS_CODE) {
			return Message.of(message.getCode(), message.getDescription());
		}
		return Message.success(task.getId());
	}
}
