package com.jj.aws.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jj.aws.dto.PersonDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonService {

    @Value("${cloud.aws.endpoint}")
    private String sqsEndpoint;

    @Autowired
    private SqsAsyncClient sqsAsyncClient;

    @Autowired
    private DynamoDbAsyncClient dynamoDbAsyncClient;

    public Mono<Void> sendPerson(PersonDto personDto) throws JsonProcessingException {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(sqsEndpoint + "/000000000000/person-queue")
                .messageBody(new JsonMapper().writeValueAsString(personDto))
                .build();
        return Mono.fromFuture(() -> sqsAsyncClient.sendMessage(request))
                .then();
    }

    @PostConstruct
    public void startListening() {
        pollMessages()
                .flatMap(this::processMessage)
                .doOnError(error -> System.out.printf("Error processing message: %s", error.getMessage()))
                .subscribe();
    }

    private Flux<Message> pollMessages() {
        return Flux.defer(this::receiveMessages)
                .flatMap(Flux::fromIterable)
                .repeatWhen(flux -> flux.delayElements(Duration.ofSeconds(10)));
    }

    private Mono<List<Message>> receiveMessages() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(sqsEndpoint + "/000000000000/person-queue")
                .maxNumberOfMessages(10)
                .waitTimeSeconds(20)
                .build();

        return Mono.fromFuture(() -> sqsAsyncClient.receiveMessage(request))
                .map(ReceiveMessageResponse::messages);
    }

    private Mono<Void> processMessage(Message message) {
        try {
            System.out.println("Received message: " + message.body());

            return savePerson(message);

        } catch (JsonProcessingException e) {
            System.out.println("ERROR: " + e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<Void> savePerson(Message message) throws JsonProcessingException {
        PersonDto personDto = new JsonMapper().readValue(message.body(), PersonDto.class);

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("cpf", AttributeValue.builder().s(personDto.cpf()).build());
        item.put("name", AttributeValue.builder().s(personDto.name()).build());
        item.put("age", AttributeValue.builder().s(String.valueOf(personDto.age())).build());

        return Mono.fromFuture(() -> dynamoDbAsyncClient.putItem(
                PutItemRequest.builder()
                        .tableName("Person")
                        .item(item)
                        .build()))
                .doOnSubscribe(sub -> {
                    System.out.println("SUB " + sub);
                })
                .doOnNext(res -> {
                    var requestID = res.responseMetadata().requestId();
                    var status = res.sdkHttpResponse().statusCode();
                    System.out.printf("PutItem, status: %d, requestId: %s%n", status, requestID);
                })
                .doOnSuccess(res -> {
                    System.out.println("SUCESSO");
                })
                .doOnError(res -> {
                    System.out.println("ERROR");
                })
                .then(deleteMessage(message.receiptHandle()));
    }

    private Mono<Void> deleteMessage(String receiptHandle) {
        DeleteMessageRequest deleteRequest = DeleteMessageRequest.builder()
                .queueUrl(sqsEndpoint + "/000000000000/person-queue")
                .receiptHandle(receiptHandle)
                .build();

        return Mono.fromFuture(() -> sqsAsyncClient.deleteMessage(deleteRequest))
                .then();
    }

    public Mono<PersonDto> findPerson(Map<String, AttributeValue> key) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName("Person")
                .key(key)
                .build();

        return Mono.fromFuture(() -> dynamoDbAsyncClient.getItem(request))
                .map(GetItemResponse::item)
                .filter(item -> !item.isEmpty())
                .map(this::mapToPerson);

    }

    public Flux<PersonDto> listAll() {
        ScanRequest scan = ScanRequest.builder()
                .tableName("Person")
                .build();

        return Flux.from(Mono.fromFuture(() -> dynamoDbAsyncClient.scan(scan))
                .flatMapMany(scanResponse -> Flux.fromIterable(scanResponse.items()))
                .map(this::mapToPerson));
    }

    private PersonDto mapToPerson(Map<String, AttributeValue> item) {
        return new PersonDto(item.get("cpf").s(),
                item.get("name").s(),
                Integer.parseInt(item.get("age").s()));
    }
}
