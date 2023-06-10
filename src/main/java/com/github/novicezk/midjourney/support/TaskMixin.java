package com.github.novicezk.midjourney.support;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public abstract class TaskMixin {
	@JsonIgnore
	private Map<String, Object> properties;
}
