package com.github.novicezk.midjourney.service.store;

import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;


public class MongoTaskStoreServiceImpl implements TaskStoreService {

    private final MongoTemplate mongoTemplate;

    public MongoTaskStoreServiceImpl(MongoTemplate mongoTemplate){
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public void save(Task task) {
        mongoTemplate.save(task);
    }

    @Override
    public void delete(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query,Task.class);
    }

    @Override
    public Task get(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query,Task.class);
    }

    @Override
    public List<Task> list() {
        return mongoTemplate.findAll(Task.class);
    }

    @Override
    public List<Task> list(TaskCondition condition) {
        return list().stream().filter(condition).toList();
    }

    @Override
    public Task findOne(TaskCondition condition) {
        return list().stream().filter(condition).findFirst().orElse(null);
    }
}
