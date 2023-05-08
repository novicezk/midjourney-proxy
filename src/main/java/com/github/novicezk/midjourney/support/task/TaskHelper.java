package com.github.novicezk.midjourney.support.task;


import java.util.List;

public interface TaskHelper {

	void putTask(String key, Task task);

	void removeTask(String key);

	Task getTask(String key);

	List<Task> listTask();

	default Task findById(String taskId) {
		return listTask().stream()
				.filter(task -> taskId.equals(task.getId()))
				.findFirst().orElse(null);
	}
}
