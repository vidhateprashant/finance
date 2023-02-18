package com.monstarbill.finance.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
	
	@Autowired
	private Environment environment;

	@Override
	public Exception decode(String methodKey, Response response) {
		switch (response.status()) {
		case 400:
			System.out.println("Bad request for the WS : " + methodKey + ".");
			// throw exception
			break;
		case 404:
			System.out.println("Request NOT found for the WS : " + methodKey + ".");
			if (methodKey.contains("getStatusList")) {
				throw new ResponseStatusException(HttpStatus.valueOf(response.status()), environment.getProperty("master.status.check"));
			}
			break;
		default:
			return new Exception(response.reason());
		}
		return null;
	}

}
