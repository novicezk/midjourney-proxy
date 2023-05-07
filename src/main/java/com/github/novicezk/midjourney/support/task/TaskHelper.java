package com.github.novicezk.midjourney.support.task;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public interface TaskHelper {
    void putTask(String key, Task task);

    void removeTask(String key);

    Task getTask(String key);

    List<Task> listTask();

    Iterator<Task> taskIterator();

    Task findById(String taskId);
}