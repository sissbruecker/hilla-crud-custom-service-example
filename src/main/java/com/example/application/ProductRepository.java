package com.example.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

//tag::snippet[]
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
}
//end::snippet[]
