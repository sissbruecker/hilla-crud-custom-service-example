package com.example.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

//tag::snippet[]
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
//end::snippet[]
