package com.motorlab.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.motorlab.entity.OrdenResumen;

public interface OrdenResumenRepository extends JpaRepository<OrdenResumen, Integer>,
        JpaSpecificationExecutor<OrdenResumen> {
}
