package com.jj.aws.controller;

import com.jj.aws.dto.PersonDto;
import com.jj.aws.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("person")
public class PersonController {

    @Autowired
    private PersonService service;

    @PostMapping
    public ResponseEntity<Void> sendPerson(@RequestBody PersonDto personDto) {
        service.sendPerson(personDto);
        return ResponseEntity.noContent().build();
    }
}
