package com.jj.aws.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jj.aws.dto.PersonDto;
import com.jj.aws.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonService service;

    @PostMapping
    public Mono<Void> sendPerson(@RequestBody PersonDto personDto) throws JsonProcessingException {
        return service.sendPerson(personDto);
    }

    @GetMapping
    public Flux<PersonDto> listAll() {
        return service.listAll();
    }

    @GetMapping("/{cpf}")
    public Mono<PersonDto> findByCpf(@PathVariable String cpf) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("cpf", AttributeValue.builder().s(cpf).build());
        return service.findPerson(key);
    }
}
