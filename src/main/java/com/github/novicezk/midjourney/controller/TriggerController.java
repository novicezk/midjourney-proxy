package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.dto.TaskDTO;
import com.github.novicezk.midjourney.dto.UVTaskDTO;
import com.github.novicezk.midjourney.enums.Action;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.service.MjDiscordService;
import com.github.novicezk.midjourney.support.MjTask;
import com.github.novicezk.midjourney.support.MjTaskHelper;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.UVData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
	private final MjDiscordService discordService;
	private final MjTaskHelper mjTaskHelper;

	@PostMapping("/submit")
	public Message<String> submitTask(@RequestBody TaskDTO taskDTO) {
		if (taskDTO.getAction() == null) {
			return Message.validationError();
		}
		if ((taskDTO.getAction() == Action.UPSCALE || taskDTO.getAction() == Action.VARIATION)
				&& (taskDTO.getIndex() < 1 || taskDTO.getIndex() > 4)) {
			return Message.validationError();
		}
		MjTask task = new MjTask();
		task.setId(RandomUtil.randomNumbers(16));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(taskDTO.getState());
		task.setAction(taskDTO.getAction());
		String key;
		Message<Void> result;
		if (Action.IMAGINE.equals(taskDTO.getAction())) {
			if (CharSequenceUtil.isBlank(taskDTO.getPrompt())) {
				return Message.validationError();
			}
			key = taskDTO.getPrompt();
			task.setPrompt(taskDTO.getPrompt());
			task.setDescription("/imagine " + taskDTO.getPrompt());
			this.mjTaskHelper.putTask(key, task);
			result = this.discordService.imagine(taskDTO.getPrompt());
		} else {
			if (CharSequenceUtil.isBlank(taskDTO.getTaskId())) {
				return Message.validationError();
			}
			MjTask targetTask = this.mjTaskHelper.findById(taskDTO.getTaskId());
			if (targetTask == null) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "任务不存在或已失效");
			}
			if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务状态错误");
			}
			task.setPrompt(targetTask.getPrompt());
			key = targetTask.getMessageId() + "-" + taskDTO.getAction();
			this.mjTaskHelper.putTask(key, task);
			if (Action.UPSCALE.equals(taskDTO.getAction())) {
				task.setDescription("/up " + taskDTO.getTaskId() + " U" + taskDTO.getIndex());
				result = this.discordService.upscale(targetTask.getMessageId(), taskDTO.getIndex(), targetTask.getMessageHash());
			} else if (Action.VARIATION.equals(taskDTO.getAction())) {
				task.setDescription("/up " + taskDTO.getTaskId() + " V" + taskDTO.getIndex());
				result = this.discordService.variation(targetTask.getMessageId(), taskDTO.getIndex(), targetTask.getMessageHash());
			} else {
				// todo 暂不支持 reset, 接收mj消息时, 无法找到对应task
				return Message.of(Message.VALIDATION_ERROR_CODE, "暂不支持 reset 操作");
			}
		}
		if (result.getCode() != Message.SUCCESS_CODE) {
			this.mjTaskHelper.removeTask(key);
			return Message.of(result.getCode(), result.getDescription());
		}
		return Message.success(task.getId());
	}

	@PostMapping("/submit-uv")
	public Message<String> submitUVTask(@RequestBody UVTaskDTO uvDTO) {
		UVData uvData = ConvertUtils.convertUVData(uvDTO.getContent());
		if (uvData == null) {
			return Message.of(Message.VALIDATION_ERROR_CODE, "/up 参数错误");
		}
		TaskDTO taskDTO = new TaskDTO();
		taskDTO.setAction(uvData.getAction());
		taskDTO.setTaskId(uvData.getId());
		taskDTO.setIndex(uvData.getIndex());
		taskDTO.setState(uvDTO.getState());
		return submitTask(taskDTO);
	}

}
