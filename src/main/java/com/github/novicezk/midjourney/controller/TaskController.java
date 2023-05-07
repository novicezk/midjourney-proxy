package com.github.novicezk.midjourney.controller;

import com.github.novicezk.midjourney.support.task.Task;
import com.github.novicezk.midjourney.support.task.TaskHelper;
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
	private final TaskHelper taskHelper;

	@GetMapping("/list")
	public List<Task> listTask() {
		return this.taskHelper.listTask();
	}

	@GetMapping("/{id}/fetch")
	public Task getTask(@PathVariable String id) {
		return this.taskHelper.findById(id);
	}

}
