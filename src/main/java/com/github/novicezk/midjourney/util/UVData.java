package com.github.novicezk.midjourney.util;

import com.github.novicezk.midjourney.enums.Action;
import lombok.Data;

@Data
public class UVData {
	private String id;
	private Action action;
	private int index;
}
