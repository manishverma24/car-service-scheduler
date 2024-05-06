package com.carservice;

import com.carservice.model.ServiceOperator;
import com.carservice.repository.ServiceOperatorRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

import java.util.Collections;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CarServiceSchedulerApplication {

	@Autowired
	private ServiceOperatorRepository serviceOperatorRepository;

	public static void main(String[] args) {
		SpringApplication.run(CarServiceSchedulerApplication.class, args);
	}

	@PostConstruct
	void createServiceOperator() {
		for (int i=0; i<3; i++) {
			ServiceOperator serviceOperator = new ServiceOperator();
			serviceOperator.setId("ServiceOperator"+i);
			serviceOperator.setName("ServiceOperatorName"+i);
			serviceOperator.setAppointments(Collections.emptyList());

			serviceOperatorRepository.save(serviceOperator);
		}
	}

}
