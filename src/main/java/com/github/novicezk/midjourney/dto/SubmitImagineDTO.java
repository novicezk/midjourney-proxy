package com.github.novicezk.midjourney.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@ApiModel("Imagine提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitImagineDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "prompt word", required = true, example = "Cat")
	private String prompt;

	@ApiModelProperty(value = "Mat map base64 array")
	private List<String> base64Array;

	@ApiModelProperty(hidden = true)
	@Deprecated(since = "3.0", forRemoval = true)
	private String base64;

}
