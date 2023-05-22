package com.github.novicezk.midjourney.service.task.service;

import com.github.novicezk.midjourney.service.task.RedisTaskServiceImpl;
import com.github.novicezk.midjourney.support.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class RedisTaskServiceImplTest {

	@Resource
	RedisTaskServiceImpl taskService;

	@Test
	public void testList() {
		for (int i = 0; i < 10; i++) {
			taskService.putTask(i + "", new Task());
		}
		List<Task> tasks = taskService.listTask();
		Assertions.assertEquals(tasks.size(), 10);
		for (int i = 0; i < 10; i++) {
			taskService.removeTask(i + "");
		}

	}
}
