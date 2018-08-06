package com.krish.hystrix.enterprise.order;

import org.springframework.stereotype.Service;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;

@Service
public class EOVHystrixMetrics {
	
	public boolean isMyCircuitOpen(HystrixCommandKey commandKey) {
		boolean isCircuitOpen = true;
		
		HystrixCircuitBreaker myCktBrkr = HystrixCircuitBreaker.Factory.getInstance(commandKey);
		if(myCktBrkr != null) {
			isCircuitOpen= myCktBrkr.isOpen();
		}
		return isCircuitOpen;
	}
}
