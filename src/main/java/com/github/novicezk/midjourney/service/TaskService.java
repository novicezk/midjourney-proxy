package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.support.Task;

import java.util.List;

public interface TaskService {

	void putTask(String id, Task task);

	void removeTask(String id);

	Task getTask(String id);

	List<Task> listTask();

}
