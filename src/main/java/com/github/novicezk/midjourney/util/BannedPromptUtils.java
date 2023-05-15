package com.github.novicezk.midjourney.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.experimental.UtilityClass;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@UtilityClass
public class BannedPromptUtils {
	private static final List<String> BANNED_WORDS;

	static {
		BANNED_WORDS = new ArrayList<>();
		var resource = BannedPromptUtils.class.getResource("/banned-words.txt");
		var lines = FileUtil.readLines(resource, StandardCharsets.UTF_8);
		for (var line : lines) {
			if (CharSequenceUtil.isBlank(line)) {
				continue;
			}
			BANNED_WORDS.add(line);
		}
	}

	public static boolean isBanned(String promptEn) {
		promptEn = promptEn.toLowerCase(Locale.ENGLISH);
		return BANNED_WORDS.stream().anyMatch(promptEn::contains);
	}

}
