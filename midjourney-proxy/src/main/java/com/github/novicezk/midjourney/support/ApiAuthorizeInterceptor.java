package com.github.novicezk.midjourney.support;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class ApiAuthorizeInterceptor implements HandlerInterceptor {
	private final ProxyProperties properties;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (CharSequenceUtil.isBlank(this.properties.getApiSecret())) {
			return true;
		}
		String apiSecret = request.getHeader(Constants.API_SECRET_HEADER_NAME);
		boolean authorized = CharSequenceUtil.equals(apiSecret, this.properties.getApiSecret());
		if (!authorized) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return authorized;
	}

}
