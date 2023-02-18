package com.monstarbill.finance.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finance")
public class TestController {
	
	@GetMapping("/status/check")
	public String getStatus() {
		return "working good from masters...";
	}
	
}
