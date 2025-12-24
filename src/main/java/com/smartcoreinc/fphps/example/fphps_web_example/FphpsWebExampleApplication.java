package com.smartcoreinc.fphps.example.fphps_web_example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;

@SpringBootApplication
@EnableAsync
public class FphpsWebExampleApplication {

	public static void main(String[] args) {
		// SQLite 데이터베이스 디렉터리 사전 생성
		File dataDir = new File("data");
		if (!dataDir.exists()) {
			dataDir.mkdirs();
			System.out.println("Created database directory: " + dataDir.getAbsolutePath());
		}

		SpringApplication.run(FphpsWebExampleApplication.class, args);
	}

}
