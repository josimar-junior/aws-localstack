package com.jj.aws.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jj.aws.dto.PersonDto;
import com.jj.aws.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonService service;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody PersonDto personDto) throws JsonProcessingException {
        service.save(personDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<PersonDto>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }
}
