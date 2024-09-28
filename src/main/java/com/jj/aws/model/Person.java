package com.jj.aws.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.jj.aws.dto.PersonDto;

@DynamoDBTable(tableName = "Person")
public class Person {

    private String cpf;
    private String name;
    private Integer age;

    public Person() {}

    public Person(String cpf, String name, Integer age) {
        this.cpf = cpf;
        this.name = name;
        this.age = age;
    }

    @DynamoDBHashKey(attributeName = "cpf")
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public PersonDto toDto() {
        return new PersonDto(cpf, name, age);
    }
}
