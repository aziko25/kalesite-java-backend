package kalesite.kalesite;

import jakarta.annotation.PostConstruct;
import kalesite.kalesite.Services.ProductsServices;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class KalesiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(KalesiteApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
	}
}