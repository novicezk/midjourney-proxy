package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskQueueHelper;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import eu.maxschuster.dataurl.DataUrl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
	private final TaskStoreService taskStoreService;
	private final DiscordService discordService;
	private final TaskQueueHelper taskQueueHelper;

	@Override
	public SubmitResultVO submitImagine(Task task, DataUrl dataUrl) {
		return this.taskQueueHelper.submitTask(task, () -> {
			if (dataUrl != null) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				String finalFileName = uploadResult.getResult();
				Message<String> sendImageResult = this.discordService.sendImageMessage("upload image: " + finalFileName, finalFileName);
				if (sendImageResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(sendImageResult.getCode(), sendImageResult.getDescription());
				}
				task.setPrompt(sendImageResult.getResult() + " " + task.getPrompt());
				task.setPromptEn(sendImageResult.getResult() + " " + task.getPromptEn());
				task.setDescription("/imagine " + task.getPrompt());
				this.taskStoreService.save(task);
			}
			return this.discordService.imagine(task.getPromptEn());
		});
	}

	@Override
	public SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		return this.taskQueueHelper.submitTask(task, () -> this.discordService.upscale(targetMessageId, index, targetMessageHash, messageFlags));
	}

	@Override
	public SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags) {
		return this.taskQueueHelper.submitTask(task, () -> this.discordService.variation(targetMessageId, index, targetMessageHash, messageFlags));
	}

	@Override
	public SubmitResultVO submitDescribe(Task task, DataUrl dataUrl) {
		return this.taskQueueHelper.submitTask(task, () -> {
			String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
			Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
			if (uploadResult.getCode() != ReturnCode.SUCCESS) {
				return Message.of(uploadResult.getCode(), uploadResult.getDescription());
			}
			String finalFileName = uploadResult.getResult();
			return this.discordService.describe(finalFileName);
		});
	}

	@Override
	public SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions) {
		return this.taskQueueHelper.submitTask(task, () -> {
			List<String> finalFileNames = new ArrayList<>();
			for (DataUrl dataUrl : dataUrls) {
				String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
				Message<String> uploadResult = this.discordService.upload(taskFileName, dataUrl);
				if (uploadResult.getCode() != ReturnCode.SUCCESS) {
					return Message.of(uploadResult.getCode(), uploadResult.getDescription());
				}
				finalFileNames.add(uploadResult.getResult());
			}
			return this.discordService.blend(finalFileNames, dimensions);
		});
	}

}
