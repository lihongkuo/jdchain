package com.jd.blockchain.gateway.web;

import java.util.List;

import com.jd.blockchain.utils.io.BytesSlice;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.jd.blockchain.crypto.asymmetric.PubKey;
import com.jd.blockchain.crypto.asymmetric.SignatureDigest;
import com.jd.blockchain.crypto.hash.HashDigest;
import com.jd.blockchain.crypto.serialize.ByteArrayObjectDeserializer;
import com.jd.blockchain.crypto.serialize.ByteArrayObjectSerializer;
import com.jd.blockchain.utils.Bytes;
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

	private static final Class<?>[] BYTEARRAY_JSON_SERIALIZE_CLASS = new Class<?>[] {
			HashDigest.class,
			PubKey.class,
			SignatureDigest.class,
			Bytes.class,
			BytesSlice.class};

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
		for (Class<?> byteArrayClass : BYTEARRAY_JSON_SERIALIZE_CLASS) {
			JSONSerializeUtils.configSerialization(byteArrayClass,
					ByteArrayObjectSerializer.getInstance(byteArrayClass),
					ByteArrayObjectDeserializer.getInstance(byteArrayClass));
		}
	}
}
