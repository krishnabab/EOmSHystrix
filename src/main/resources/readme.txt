Access URL:
http://localhost.att.com:9091/eo/create

Hystrix GUI : 
http://localhost:9091/eo/hystrix/monitor?stream=http%3A%2F%2Flocalhost%3A9091%2Feo%2Fhystrix.stream&title=HystrixDB



    Timeout for every request to an external system (default: 1000 ms)

    Limit of concurrent requests for external system (default: 10)

    Circuit breaker to avoid further requests (default: when more than 50% of all requests fail)

    Retry of a single request after circuit breaker has triggered (default: every 5 seconds)

    Interfaces to retrieve runtime information on request and aggregate level (there’s even a ready-to-use realtime dashboard for it)

