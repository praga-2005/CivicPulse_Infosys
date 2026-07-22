package com.infosys.CivicsApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
			.route("citizen-service", r -> r.path("/api/citizens/**").uri("lb://CITIZEN-SERVICE"))
			.route("department-service", r -> r.path("/api/departments/**").uri("lb://DEPARTMENT-SERVICE"))
			.route("officer-service", r -> r.path("/api/officers/**").uri("lb://OFFICER-SERVICE"))
			.route("grievance-service", r -> r.path("/api/grievances/**").uri("lb://GRIEVANCE-SERVICE"))
			.route("complaints-service", r -> r.path("/api/complaints/**").uri("lb://GRIEVANCE-SERVICE"))
			.route("admin-service", r -> r.path("/api/admin/**").uri("lb://GRIEVANCE-SERVICE"))
			.route("service-management", r -> r.path("/api/services/**").uri("lb://SERVICE-MANAGEMENT-SERVICE"))
			.build();
	}

}
