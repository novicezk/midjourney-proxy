import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.support.TaskRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MySQLTaskStoreServiceImpl implements TaskStoreService {

    private final TaskRepository taskRepository;

    @Autowired
    public MySQLTaskStoreServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    @Override
    public void save(Task task) {
        taskRepository.save(task);
    }

    @Override
    public void delete(String id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Task get(String id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Override
    public List<Task> list() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> list(TaskCondition condition) {
        return taskRepository.findAll().stream()
                .filter(task -> condition.test(task))
                .collect(Collectors.toList());
    }

    @Override
    public Task findOne(TaskCondition condition) {
        return taskRepository.findAll().stream()
                .filter(task -> condition.test(task))
                .findFirst().orElse(null);
    }
}
