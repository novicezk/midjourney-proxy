package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.support.task.Task;

public interface NotifyService {

	void notifyTaskChange(Task task);

}
