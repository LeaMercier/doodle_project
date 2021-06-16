package fr.istic.tlc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import org.springframework.http.HttpMethod;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	RouteLocator gateway(RouteLocatorBuilder builder) {

		return builder.routes()
				.route(routeSpec -> routeSpec
						.path("api/users/*/choices/**").and().method(HttpMethod.GET)
						.uri("http://poll:8083"))
		// participant service
				.route(routeSpec -> routeSpec
						.path("*/api/users/**")
						.uri("http://participant:8081"))
				.route(routeSpec -> routeSpec
						.path("api/polls/*/mealpreferences")
						.uri("http://participant:8081"))
				.route(routeSpec -> routeSpec
						.path("api/polls/*/mealpreference/**")
						.uri("http://participant:8081"))
		// planning service
				.route(routeSpec -> routeSpec
						.path("api/choices/**")
						.uri("http://planning:8082"))
		// poll service
				.route(routeSpec -> routeSpec
						.path("api/polls/**")
						.uri("http://poll:8083"))
				.route(routeSpec -> routeSpec
						.path("api/poll/**")
						.uri("http://poll:8083"))
				.build();
	}

}
