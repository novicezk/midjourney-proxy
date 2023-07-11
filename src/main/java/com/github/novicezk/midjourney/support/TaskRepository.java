package com.github.novicezk.midjourney.support;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.novicezk.midjourney.support.Task;

public interface TaskRepository extends JpaRepository<Task, String> {
}
