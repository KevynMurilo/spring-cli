package com.example.demo.application.usecase;

import com.example.demo.domain.model.Demo;
import com.example.demo.domain.repository.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DemoUseCase {

    private final DemoRepository repository;

    public List<Demo> findAll() {
        return repository.findAll();
    }

    public Optional<Demo> findById(Long id) {
        return repository.findById(id);
    }

    public Demo create(Demo entity) {
        return repository.save(entity);
    }

    public Demo update(Long id, Demo entity) {
        return repository.findById(id)
                .map(existing -> {
                    entity.setId(id);
                    return repository.save(entity);
                })
                .orElseThrow(() -> new RuntimeException("Demo not found with id: " + id));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}