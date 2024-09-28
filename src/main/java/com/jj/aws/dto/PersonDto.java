package com.jj.aws.dto;

import com.jj.aws.model.Person;

public record PersonDto(String cpf, String name, Integer age) {

    public Person toModel() {
        return new Person(cpf, name, age);
    }
}
