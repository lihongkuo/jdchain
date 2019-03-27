package com.jd.blockchain.gateway.web;

import java.util.List;

import com.jd.blockchain.web.serializes.ByteArrayObjectUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jd.blockchain.utils.io.ByteArray;
import com.jd.blockchain.utils.serialize.json.JSONSerializeUtils;
import com.jd.blockchain.utils.web.model.JsonWebResponseMessageConverter;
import com.jd.blockchain.web.converters.BinaryMessageConverter;
import com.jd.blockchain.web.converters.HashDigestInputConverter;

/**
 * @author zhuguang
 * @date 2018-08-08
 */
@Configuration
public class GatewayWebServerConfigurer implements WebMvcConfigurer {

	static {
		JSONSerializeUtils.disableCircularReferenceDetect();
		JSONSerializeUtils.configStringSerializer(ByteArray.class);
	}


	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		int index = converters.size();
		for (int i = 0; i < converters.size(); i++) {
			if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
				index = i;
				break;
			}
		}

		JsonWebResponseMessageConverter jsonConverter = new JsonWebResponseMessageConverter(false);

		converters.add(index, jsonConverter);

		converters.add(0, new BinaryMessageConverter());

		initByteArrayJsonSerialize();
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new HashDigestInputConverter());
	}

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**")
				.addResourceLocations("classpath:/META-INF/resources");
    }

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/").setViewName("web/index.html");
	}

	private void initByteArrayJsonSerialize() {
		ByteArrayObjectUtil.init();
	}
}
