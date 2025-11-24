package com.example.demo.infrastructure.persistence;

import com.example.demo.domain.model.Demo;
import com.example.demo.domain.repository.DemoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DemoRepositoryImpl implements DemoRepository {

    private final DemoJpaRepository jpaRepository;

    @Override
    public List<Demo> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Demo> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Demo save(Demo domain) {
        DemoEntity entity = toEntity(domain);
        DemoEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Demo toDomain(DemoEntity entity) {
        return Demo.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }

    private DemoEntity toEntity(Demo domain) {
        return DemoEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .build();
    }
}