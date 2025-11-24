package com.example.demo.domain.repository;

import com.example.demo.domain.model.Demo;
import java.util.List;
import java.util.Optional;

public interface DemoRepository {

    List<Demo> findAll();

    Optional<Demo> findById(Long id);

    Demo save(Demo entity);

    void deleteById(Long id);
}