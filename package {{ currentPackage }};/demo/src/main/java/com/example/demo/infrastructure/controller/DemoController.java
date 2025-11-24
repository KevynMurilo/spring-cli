package com.example.demo.infrastructure.controller;

import com.example.demo.application.usecase.DemoUseCase;
import com.example.demo.domain.model.Demo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demos")
@RequiredArgsConstructor
@Tag(name = "Demo", description = "Demo management APIs")
public class DemoController {

    private final DemoUseCase useCase;

        @Operation(summary = "Get all demos")
        @GetMapping
    public ResponseEntity<List<Demo>> getAll() {
        return ResponseEntity.ok(useCase.findAll());
    }

        @Operation(summary = "Get demo by ID")
        @GetMapping("/{id}")
    public ResponseEntity<Demo> getById(@PathVariable Long id) {
        return useCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

        @Operation(summary = "Create new demo")
        @PostMapping
    public ResponseEntity<Demo> create(@RequestBody Demo entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(entity));
    }

        @Operation(summary = "Update demo")
        @PutMapping("/{id}")
    public ResponseEntity<Demo> update(@PathVariable Long id, @RequestBody Demo entity) {
        return ResponseEntity.ok(useCase.update(id, entity));
    }

        @Operation(summary = "Delete demo")
        @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }
}