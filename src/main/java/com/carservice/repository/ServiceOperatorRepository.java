package com.carservice.repository;

import com.carservice.model.ServiceOperator;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceOperatorRepository extends MongoRepository<ServiceOperator, String> {

    Optional<ServiceOperator> findById(String operatorId);
}


