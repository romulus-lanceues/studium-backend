package com.lancea.studium.studium_api;

import com.lancea.studium.studium_api.service.RedisTestService;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.crypto.SecretKey;
import java.util.Base64;

@SpringBootApplication
public class StudiumApiApplication implements CommandLineRunner {

	@Autowired
	private RedisTestService redisTestService;

	public static void main(String[] args) {
		SpringApplication.run(StudiumApiApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception{
		redisTestService.testConnection();
	}


}
