package com.github.novicezk.midjourney.controller;

import com.github.novicezk.midjourney.support.MjTask;
import com.github.novicezk.midjourney.support.MjTaskHelper;
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
	private final MjTaskHelper mjTaskHelper;

	@GetMapping("/list")
	public List<MjTask> listTask() {
		return this.mjTaskHelper.listTask();
	}

	@GetMapping("/{id}/fetch")
	public MjTask getTask(@PathVariable String id) {
		return this.mjTaskHelper.findById(id);
	}

}
