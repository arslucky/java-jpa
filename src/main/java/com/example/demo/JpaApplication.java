package com.example.demo;

import com.example.demo.repository.CustomerRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class JpaApplication {
	private static ApplicationContext ctx;

	public static void main(String[] args) {
		ctx = SpringApplication.run(JpaApplication.class, args);
//		DemoApplication demoApplication = new DemoApplication();
//		demoApplication.test();
	}

	public void test() {
		var res = ctx.getBean(CustomerRepository.class).findAll();
		System.out.println(res);
	}
}
