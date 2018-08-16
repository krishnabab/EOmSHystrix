package com.krish.hystrix.enterprise.order;


import javax.ws.rs.InternalServerErrorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.exception.HystrixBadRequestException;

@EnableHystrix
@EnableCircuitBreaker
@EnableHystrixDashboard
@Service
public class CallMicroServices {
	
	@Autowired
    RestTemplate restTemplate;
 
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
 
    @Bean
    public AlwaysSampler alwaysSampler() {
        return new AlwaysSampler();
    }

	@Value("${eov.url}")
	private String eovURL;
	
	private static final Logger log = LoggerFactory.getLogger(CallMicroServices.class.getName());

	
	@HystrixCommand(threadPoolKey="thread", fallbackMethod = "fallBack4Retry", commandKey = "eovCommand",commandProperties = {
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE"),
			@HystrixProperty(name="execution.isolation.semaphore.maxConcurrentRequests", value= "200"),
			@HystrixProperty(name="execution.timeout.enabled",value="false"),
			@HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="2"),
			@HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value="5")
	})
	public String callEOV() throws Exception {
		String response = "Order not completed";
		String response2 = null;
		try {
			log.info("in EO and calling EOV mS: " + System.currentTimeMillis());
			
			//int temp = 9/0; // to create ArithmeticException
			//generate runtime exception
			//int s[] = {1,2};
			//System.out.println("dddd"+s[3]);
			/*HttpUriRequest request = new HttpGet(eovURL);
			RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1 * 500).build();
			HttpResponse httpResponse = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build().execute(request);
			HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
			response = EntityUtils.toString(httpResponse.getEntity());*/
			response2 = (String) restTemplate.exchange(eovURL,HttpMethod.GET, null, new ParameterizedTypeReference<String>() {}).getBody();
		} catch(ArithmeticException ae) {
			throw new HystrixBadRequestException(ae.getMessage()); 
			/*
			 * All exceptions thrown from the run() method
			 * except for HystrixBadRequestException count as failures and trigger
			 * getFallback() and circuit-breaker logic. The HystrixBadRequestException is
			 * intended for use cases such as reporting illegal arguments or non-system
			 * failures that should not count against the failure metrics and should not
			 * trigger fallback logic.
			 *  
			 */
		}catch(Exception e) {
			log.info("in the catch:"+e.getClass().getName()+":"+e.getMessage());
			throw e;
		}
		
		if (response2.contains("Validated"))
			response2 = "Order Created and completed!!";
		else
			throw new InternalServerErrorException();
		return response2;
	}

	public String fallBack4Retry(Throwable e) {
			/*
			 *  Either Give user some valid message about error 
			 *  or
			 *  Retry once and fail and then display message 
			 */
			String resp = e.getMessage();
			log.info("RESP>>"+e.getClass().getName()+":"+e.getMessage());
			try {
				//resp = callEOV();
			} catch (Exception e1) {
				resp="Even after retry we got issue:"+e1.getMessage();
				log.info(resp);
			}
			return resp;
	}
}
