package UnitSystem.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableCaching
@EnableWebSocketMessageBroker
public class HelwanApplication {
//TODO: the user do not retrive all data to the dashboard and the data is not updated whend the user enroll in a course
	public static void main(String[] args) {
		SpringApplication.run(HelwanApplication.class, args);
	}

}
