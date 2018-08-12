package com.krish.hystrix.enterprise.order;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.InternalServerErrorException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;

@EnableHystrix
@EnableCircuitBreaker
@EnableHystrixDashboard
@Service
public class CallMicroServices {

	@Value("${eov.url}")
	private String eovURL;
	
	private static final Logger log = LoggerFactory.getLogger(CallMicroServices.class);

	
	@HystrixCommand(fallbackMethod = "fallBack4Retry", commandKey = "eovCommand",commandProperties = {
			@HystrixProperty(name="execution.timeout.enabled",value="false"),
			@HystrixProperty(name="execution.isolation.semaphore.maxConcurrentRequests", value= "200"),
			@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="600000"),
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE"),	
			@HystrixProperty(name="fallback.enabled", value="true"),
			@HystrixProperty(name="fallback.isolation.semaphore.maxConcurrentRequests", value="100"),
			@HystrixProperty(name="circuitBreaker.enabled", value="true"),
			@HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="2"),
			@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="5000"),
			@HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="50"),
	})
	/*
	 * @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="20")
	 * Over 20 % failure rate in 10 sec period, open breaker
	 * 
	 * @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",
	 * value="1000") After 1 second , close the circuit breaker
	 * 
	 * @HystrixCommand(fallbackMethod = "autoValidate", commandProperties = {
	 * 
	 * @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value =
	 * "20"),
	 * 
	 * @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value =
	 * "1000") })
	 */
	public String callEOV() throws Exception {
		String response = "Order not completed";
		try {
			log.info("in EO and calling EOV mS: " + System.currentTimeMillis());
			//int temp = 9/0; // to create ArithmeticException
			//generate runtime exception
			//int s[] = {1,2};
			//System.out.println("dddd"+s[3]);
			HttpUriRequest request = new HttpGet(eovURL);
			//RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1 * 500).build();
			//HttpResponse httpResponse = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build().execute(request);
			HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);
			response = EntityUtils.toString(httpResponse.getEntity());
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
		
		if (response.contains("Validated"))
			response = "Order Created and completed!!";
		else
			throw new InternalServerErrorException();
		return response;
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
