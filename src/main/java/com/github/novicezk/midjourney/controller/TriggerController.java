package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.dto.TaskDTO;
import com.github.novicezk.midjourney.enums.TaskType;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.service.MjDiscordService;
import com.github.novicezk.midjourney.support.MjTask;
import com.github.novicezk.midjourney.support.MjTaskHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
	private final MjDiscordService discordService;
	private final MjTaskHelper mjTaskHelper;

	@PostMapping("/submit")
	public Message<Void> submitTask(@RequestBody TaskDTO taskDTO) {
		if (!CharSequenceUtil.isAllNotBlank(taskDTO.getRoom(), taskDTO.getUser(), taskDTO.getPrompt())) {
			return Message.validationError();
		}
		MjTask task = new MjTask();
		task.setSubmitDate(new Date());
		task.setRoom(taskDTO.getRoom());
		task.setUser(taskDTO.getUser());
		task.setType(taskDTO.getType());
		String key;
		Message<Void> result;
		if (TaskType.IMAGINE.equals(taskDTO.getType())) {
			key = taskDTO.getPrompt();
			task.setKey(key);
			task.setPrompt(taskDTO.getPrompt());
			this.mjTaskHelper.putTask(key, task);
			result = this.discordService.imagine(taskDTO.getPrompt());

		} else if (TaskType.UP.equals(taskDTO.getType())) {
			UpData upData = convertUpData(taskDTO.getPrompt());
			if (upData == null) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "/up 参数错误");
			}
			MjTask targetTask = this.mjTaskHelper.findByTypeAndMessageId(TaskType.IMAGINE, upData.getMessageId());
			if (targetTask == null) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "ID不存在");
			}
			if (CharSequenceUtil.isBlank(targetTask.getMessageHash())) {
				return Message.of(Message.VALIDATION_ERROR_CODE, "关联任务尚未完成，请等待");
			}
			key = upData.getMessageId() + "-" + upData.getAction().charAt(0);
			task.setKey(key);
			task.setPrompt(targetTask.getPrompt());
			task.setMessageId(upData.getMessageId());
			task.setMessageHash(targetTask.getMessageHash());
			this.mjTaskHelper.putTask(key, task);
			result = this.discordService.up(upData.getMessageId(), upData.getAction(), upData.getIndex(), targetTask.getMessageHash());
		} else {
			return Message.of(Message.VALIDATION_ERROR_CODE, taskDTO.getType() + " Not Supported");
		}
		if (result.getCode() != Message.SUCCESS_CODE) {
			this.mjTaskHelper.removeTask(key);
		}
		return result;
	}

	@GetMapping("/list-task")
	public List<MjTask> listTask() {
		return this.mjTaskHelper.listTask();
	}

	private UpData convertUpData(String text) {
		List<String> split = CharSequenceUtil.split(text, " ");
		if (split.size() != 2) {
			return null;
		}
		String action = split.get(1).toLowerCase();
		if (action.length() != 2) {
			return null;
		}
		UpData upData = new UpData();
		if (action.charAt(0) == 'u') {
			upData.setAction("upsample");
		} else if (action.charAt(0) == 'v') {
			upData.setAction("variation");
		} else {
			return null;
		}
		try {
			int index = Integer.parseInt(action.substring(1, 2));
			if (index > 4) {
				return null;
			}
			upData.setIndex(index);
		} catch (NumberFormatException e) {
			return null;
		}
		upData.setMessageId(split.get(0));
		return upData;
	}

	@Data
	static class UpData {
		private String messageId;
		private String action;
		private int index;
	}
}
