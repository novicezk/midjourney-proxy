package com.github.novicezk.midjourney.controller;

import java.net.MalformedURLException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Resource;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.github.novicezk.midjourney.service.thread.MjTask;
import com.github.novicezk.midjourney.support.BannedPromptHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import com.github.novicezk.midjourney.util.UVData;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;

@Api(tags = "任务提交")
@RestController
@RequestMapping("/trigger")
@RequiredArgsConstructor
public class TriggerController {
    private final DiscordService discordService;
    private final TranslateService translateService;
    private final TaskService taskService;
    private final ProxyProperties properties;
    private final BannedPromptHelper bannedPromptHelper;

    @Resource
    private Executor taskExecutor;

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
        task.setNotifyHook(CharSequenceUtil.isBlank(submitDTO.getNotifyHook()) ? this.properties.getNotifyHook()
            : submitDTO.getNotifyHook());
        task.setId(RandomUtil.randomNumbers(16));
        task.setState(submitDTO.getState());
        task.setAction(submitDTO.getAction());
        MjTask mjTask;
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
                promptEn = this.translateService.translateToEnglish(prompt.substring(0, paramStart)).trim()
                    + prompt.substring(paramStart);
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
            mjTask = new MjTask(this.discordService, this.taskService, task, null, null, null, null, Action.IMAGINE);
        } else {
            if (CharSequenceUtil.isBlank(submitDTO.getTaskId())) {
                return Message.validationError();
            }
            Task targetTask = this.taskService.getTask(submitDTO.getTaskId());
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
            String key = targetTask.getMessageId() + "-" + submitDTO.getAction();
            task.setKey(key);
            if (Action.UPSCALE.equals(submitDTO.getAction())) {
                task.setDescription("/up " + submitDTO.getTaskId() + " U" + submitDTO.getIndex());
                this.taskService.putTask(task.getId(), task);
                mjTask = new MjTask(this.discordService, this.taskService, task, targetTask, submitDTO.getIndex(), null,
                    null, Action.UPSCALE);
            } else if (Action.VARIATION.equals(submitDTO.getAction())) {
                task.setDescription("/up " + submitDTO.getTaskId() + " V" + submitDTO.getIndex());
                this.taskService.putTask(task.getId(), task);
                mjTask = new MjTask(this.discordService, this.taskService, task, targetTask, submitDTO.getIndex(), null,
                    null, Action.VARIATION);
            } else {
                return Message.of(Message.VALIDATION_ERROR_CODE, "不支持的操作");
            }
        }
        return submitMjTask(mjTask);
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
        task.setId(RandomUtil.randomNumbers(16));
        String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
        task.setState(describeDTO.getState());
        task.setAction(Action.DESCRIBE);
        task.setDescription("/describe " + taskFileName);
        task.setKey(taskFileName);
        task.setNotifyHook(CharSequenceUtil.isBlank(describeDTO.getNotifyHook()) ? this.properties.getNotifyHook()
            : describeDTO.getNotifyHook());
        this.taskService.putTask(task.getId(), task);
        MjTask mjTask =
            new MjTask(this.discordService, this.taskService, task, null, null, taskFileName, dataUrl, Action.DESCRIBE);
        return submitMjTask(mjTask);
    }

    private Message<String> submitMjTask(MjTask mjTask) {
        int size = 0;
        try {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor)taskExecutor;
            size = executor.getThreadPoolExecutor().getQueue().size();
            this.taskExecutor.execute(mjTask);
        } catch (RejectedExecutionException e) {
            this.taskService.removeTask(mjTask.getTask().getId());
            return Message.failure("队列已满，请稍后尝试");
        }
        if (size == 0) {
            return Message.success(mjTask.getTask().getId());
        } else {
            return Message.success(Message.WAITING_CODE, "排队中，您的前面还有" + size + "人", mjTask.getTask().getId());
        }
    }
}
