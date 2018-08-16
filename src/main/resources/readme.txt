Access URL:
http://localhost.att.com:9091/eo/create

Hystrix GUI : 
http://localhost:9091/eo/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A9091%2Feo%2Fhystrix.stream&title=HystrixDB



    Timeout for every request to an external system (default: 1000 ms)

    Limit of concurrent requests for external system (default: 10)

    Circuit breaker to avoid further requests (default: when more than 50% of all requests fail)

    Retry of a single request after circuit breaker has triggered (default: every 5 seconds)

    Interfaces to retrieve runtime information on request and aggregate level (there’s even a ready-to-use realtime dashboard for it)




@HystrixProperty(name="execution.timeout.enabled",value="false"),
			@HystrixProperty(name="execution.isolation.semaphore.maxConcurrentRequests", value= "200"),
			@HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds", value="600000"),
			@HystrixProperty(name="execution.isolation.strategy", value="SEMAPHORE"),	
			@HystrixProperty(name="fallback.enabled", value="true"),
			@HystrixProperty(name="fallback.isolation.semaphore.maxConcurrentRequests", value="100"),
			@HystrixProperty(name="circuitBreaker.enabled", value="true"),
			@HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="2"),
			@HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="5000"),
			
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