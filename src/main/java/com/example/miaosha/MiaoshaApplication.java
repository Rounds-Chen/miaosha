package com.example.miaosha;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.example.miaosha"})
@RestController
@EnableScheduling
@MapperScan("com.example.miaosha.dao")
public class MiaoshaApplication {

	@RequestMapping("/")
	public String home(){
		return "hello world";
	}

	public static void main(String[] args) {
		SpringApplication.run(MiaoshaApplication.class, args);
	}

}
