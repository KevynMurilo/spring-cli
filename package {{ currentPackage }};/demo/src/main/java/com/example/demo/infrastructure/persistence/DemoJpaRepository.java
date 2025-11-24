package com.example.demo.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DemoJpaRepository extends JpaRepository<DemoEntity, Long> {
}