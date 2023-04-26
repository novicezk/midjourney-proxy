package com.github.novicezk.midjourney.service.translate;


import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.MD5;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.service.TranslateService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class BaiduTranslateServiceImpl implements TranslateService {
	private static final String TRANSLATE_API = "https://fanyi-api.baidu.com/api/trans/vip/translate";

	private final String appid;
	private final String appSecret;

	public BaiduTranslateServiceImpl(ProxyProperties.BaiduTranslateConfig translateConfig) {
		this.appid = translateConfig.getAppid();
		this.appSecret = translateConfig.getAppSecret();
		if (!CharSequenceUtil.isAllNotBlank(this.appid, this.appSecret)) {
			throw new BeanDefinitionValidationException("mj-proxy.baidu-translate.appid或mj-proxy.baidu-translate.app-secret未配置");
		}
	}

	@Override
	public String translateToEnglish(String prompt) {
		if (!containsChinese(prompt)) {
			return prompt;
		}
		String salt = RandomUtil.randomNumbers(5);
		String sign = MD5.create().digestHex(this.appid + prompt + salt + this.appSecret);
		String url = TRANSLATE_API + "?from=zh&to=en&appid=" + this.appid + "&salt=" + salt + "&q=" + prompt + "&sign=" + sign;
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().getForEntity(url, String.class);
			if (responseEntity.getStatusCode() != HttpStatus.OK || CharSequenceUtil.isBlank(responseEntity.getBody())) {
				throw new ValidateException(responseEntity.getStatusCodeValue() + " - " + responseEntity.getBody());
			}
			JSONObject result = new JSONObject(responseEntity.getBody());
			if (result.has("error_code")) {
				throw new ValidateException(result.getString("error_code") + " - " + result.getString("error_msg"));
			}
			return result.getJSONArray("trans_result").getJSONObject(0).getString("dst");
		} catch (Exception e) {
			log.warn("调用百度翻译失败: {}", e.getMessage());
		}
		return prompt;
	}

}
