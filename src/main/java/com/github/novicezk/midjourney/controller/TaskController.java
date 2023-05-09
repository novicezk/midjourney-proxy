package com.github.novicezk.midjourney.controller;

import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.support.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
	private final TaskService taskService;

	@GetMapping("/list")
	public List<Task> listTask() {
		return this.taskService.listTask();
	}

	@GetMapping("/{id}/fetch")
	public Task getTask(@PathVariable String id) {
		return this.taskService.getTask(id);
	}

}
