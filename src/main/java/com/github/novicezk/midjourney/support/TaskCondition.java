package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.function.Predicate;


@Data
@Accessors(chain = true)
public class TaskCondition implements Predicate<Task> {
	private String id;

	private Set<TaskStatus> statusSet;
	private Set<TaskAction> actionSet;

	private String prompt;
	private String promptEn;
	private String description;

	private String relatedTaskId;
	private String messageId;
	private String progressMessageId;

	@Override
	public boolean test(Task task) {
		if (task == null) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.id) && !this.id.equals(task.getId())) {
			return false;
		}
		if (this.statusSet != null && !this.statusSet.isEmpty() && !this.statusSet.contains(task.getStatus())) {
			return false;
		}
		if (this.actionSet != null && !this.actionSet.isEmpty() && !this.actionSet.contains(task.getAction())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.prompt) && !this.prompt.equals(task.getPrompt())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.promptEn) && !this.promptEn.equals(task.getPromptEn())) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.description) && !this.description.equals(task.getDescription())) {
			return false;
		}

		if (CharSequenceUtil.isNotBlank(this.relatedTaskId) && !this.relatedTaskId.equals(task.getProperty(Constants.TASK_PROPERTY_RELATED_TASK_ID))) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.messageId) && !this.messageId.equals(task.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID))) {
			return false;
		}
		if (CharSequenceUtil.isNotBlank(this.progressMessageId) && !this.progressMessageId.equals(task.getProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID))) {
			return false;
		}
		return true;
	}

}
