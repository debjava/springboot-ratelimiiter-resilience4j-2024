package com.ddlab.rnd.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
public class TempController {

	@RateLimiter(name = "infoCall1", fallbackMethod = "rateLimitingFallback")
	@GetMapping(path = "/v1/info/{id}")
	public ResponseEntity<String> getInfo(@PathVariable String id) {
		System.out.println("Input ID: " + id);
		return new ResponseEntity<>("Some Value", HttpStatus.OK);
	}

	public ResponseEntity<String> rateLimitingFallback(String id, RequestNotPermitted ex) {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Retry-After", "60s"); // retry after one second
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(responseHeaders) // send retry header
				.body("Too Many Requests - Retry After 1 Minute");
	}

	// For BulkHead
	@GetMapping(value = "/course/{id}")
	@Bulkhead(name = "courseBulkheadApi", fallbackMethod = "bulkheadFallback")
	public ResponseEntity<String> getCourse(@PathVariable String id) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>("Some course", HttpStatus.OK);
	}

	public ResponseEntity bulkheadFallback(String id, BulkheadFullException ex) {
		return ResponseEntity.status(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED)
				.body("Too many request - No further calls are accepted");
	}

}
