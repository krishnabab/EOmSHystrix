package com.krish.hystrix.enterprise.order;

import javax.annotation.PostConstruct;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.hystrix.HystrixCommandKey;

@RestController
public class EOController {

	@Autowired
	private CallMicroServices callmS;
	
	@Autowired
	private EOVHystrixMetrics metricsService; 

	@RequestMapping("/create")
	@Produces({ MediaType.TEXT_PLAIN })
	public String createOrder() throws Exception {
		
		String resp = null;
		resp = callmS.callEOV();
		/*
		 *  Now check the circuit is open or not ?
		 *  if circuit is open dont execute write to DB operation
		 
		if (metricsService.isMyCircuitOpen(HystrixCommandKey.Factory.asKey("eovCommand"))) {
			
			// You should have a scheduler service which will try EOV calls in the background
			 
			resp = "Network glitch!! we r working , please re-try after some time";
			System.out.println("Circuit is open and something is not good ! Can't write into DB");
		}
		else {
			 //Circuit is not open , so please write to DB and proceed with notmal flow  
			System.out.println("Step -1 : DB Record created");
			resp = callmS.callEOV();
		}*/
		return resp;
	}
	
	@PostConstruct
	/**
	 * First step after application startup is to warmup Hystrix circuit breaker 
	 * by sending some dummy call , so that we will be able to know the active 
	 * status of Hystrix circuit breaker from next step on wards 
	 */
    public void initHystrix() throws Exception {
        System.out.println("Initializing circuit breaker by sending dummy request");
        // A dummy call to EOV
     	callmS.callEOV();
    }
	
	//@Scheduled(cron ="${callEOVCron.expression}")
	public void processDBRecords() throws Exception {
	   System.out.println("Processing DB Records");
	   callmS.callEOV();
	}
}