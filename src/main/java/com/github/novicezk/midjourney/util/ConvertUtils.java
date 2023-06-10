package com.github.novicezk.midjourney.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.enums.TaskAction;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ConvertUtils {

	public static TaskChangeParams convertChangeParams(String content) {
		List<String> split = CharSequenceUtil.split(content, " ");
		if (split.size() != 2) {
			return null;
		}
		String action = split.get(1).toLowerCase();
		TaskChangeParams changeParams = new TaskChangeParams();
		if (action.charAt(0) == 'u') {
			changeParams.setAction(TaskAction.UPSCALE);
		} else if (action.charAt(0) == 'v') {
			changeParams.setAction(TaskAction.VARIATION);
		} else if (action.equals("r")) {
			changeParams.setAction(TaskAction.REROLL);
		} else {
			return null;
		}
		try {
			int index = Integer.parseInt(action.substring(1, 2));
			if (index < 1 || index > 4) {
				return null;
			}
			changeParams.setIndex(index);
		} catch (NumberFormatException e) {
			return null;
		}
		changeParams.setId(split.get(0));
		return changeParams;
	}

}
