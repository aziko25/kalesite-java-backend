package kalesite.kalesite;

import jakarta.annotation.PostConstruct;
import kalesite.kalesite.Services.ProductsServices;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

@SpringBootApplication
public class KalesiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(KalesiteApplication.class, args);
	}

	@Bean
	public ApplicationRunner applicationRunner(ProductsServices productsServices) {

		return args -> productsServices.insertFetchedProducts();
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
	}
}