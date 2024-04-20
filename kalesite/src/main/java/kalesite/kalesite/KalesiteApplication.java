package kalesite.kalesite;

import jakarta.annotation.PostConstruct;
import kalesite.kalesite.Models.Payme.Entities.CustomerOrder;
import kalesite.kalesite.Repositories.Payme.OrderRepository;
import org.springframework.boot.CommandLineRunner;
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

	@Bean
	public CommandLineRunner prepare(OrderRepository repository) {

		return args -> {
			repository.save(new CustomerOrder("1", 50000, true));
			repository.save(new CustomerOrder("2", 55000, false));
			repository.save(new CustomerOrder("3", 60000, false));
		};
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
	}
}