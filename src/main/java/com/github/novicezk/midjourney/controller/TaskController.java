package com.github.novicezk.midjourney.controller;

import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "任务查询")
@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
	private final TaskService taskService;

	@ApiOperation(value = "列出所有任务信息")
	@GetMapping("/list")
	public List<Task> listTask() {
		return this.taskService.listTask();
	}

	@ApiOperation(value = "列出指定id任务信息")
	@GetMapping("/{id}/fetch")
	public Task getTask(@ApiParam(value = "任务id") @PathVariable String id) {
		return this.taskService.getTask(id);
	}

}
