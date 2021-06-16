package fr.istic.tlc.pad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories

//@ComponentScan(basePackages = {"fr.istic.tlc.pad"})
public class PadApplication {

	public static void main(String[] args) {
		SpringApplication.run(PadApplication.class, args);
	}

}
