package com.jj.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jj.aws.dto.PersonDto;
import com.jj.aws.repository.PersonRepository;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PersonService {

    @Autowired
    private SqsTemplate sqsTemplate;

    @Autowired
    private PersonRepository personRepository;

    public void sendPerson(PersonDto personDto) {
        sqsTemplate.send(to -> {
                to.queue("person-queue").payload(personDto);
        });
    }

    @SqsListener("person-queue")
    public void listenerSendPerson(@Payload PersonDto personDto) {
        System.out.println("person saved: " + personDto);

        personRepository.save(personDto.toModel());
    }

    public PersonDto findByCpf(String cpf) {
        return personRepository.findById(cpf).orElseThrow(() -> new RuntimeException("Person not found")).toDto();
    }
}
