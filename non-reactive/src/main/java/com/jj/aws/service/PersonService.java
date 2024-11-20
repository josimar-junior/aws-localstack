package com.jj.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jj.aws.dto.PersonDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonService {

    @Value("${cloud.aws.endpoint}")
    private String endpoint;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    private DynamoDbClient dynamoDbClient;

    public void save(PersonDto personDto) throws JsonProcessingException {
        var request = SendMessageRequest.builder()
                .queueUrl(endpoint + "/000000000000/person-queue")
                .messageBody(new JsonMapper().writeValueAsString(personDto))
                .build();
        sqsClient.sendMessage(request);
    }

    public void listener() {

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(endpoint + "/000000000000/person-queue")
                .maxNumberOfMessages(3)
                .waitTimeSeconds(10)
                .build();

        sqsClient.receiveMessage(request).messages().forEach(message -> {
            try {
                processMessage(message);
                deleteMessage(message);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void processMessage(Message message) throws JsonProcessingException {
        System.out.printf("Person saved: %s%n", message.body());
        var person = new JsonMapper().readValue(message.body(), PersonDto.class);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("cpf", AttributeValue.builder().s(person.cpf()).build());
        item.put("name", AttributeValue.builder().s(person.name()).build());
        item.put("age", AttributeValue.builder().s(String.valueOf(person.age())).build());

        dynamoDbClient.putItem(PutItemRequest.builder()
                        .tableName("Person")
                        .item(item)
                .build());
    }

    public List<PersonDto> listAll() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName("Person")
                .build();

        return dynamoDbClient.scan(scanRequest)
                .items().stream()
                .map(item -> new PersonDto(item.get("cpf").s(), item.get("name").s(), Integer.parseInt(item.get("age").s())))
                .toList();
    }

    public void deleteMessage(Message message) {
        sqsClient.deleteMessage(mess -> mess.queueUrl(endpoint + "/000000000000/person-queue").receiptHandle(message.receiptHandle()));
    }
}
