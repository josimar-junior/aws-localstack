package com.jj.aws.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduleListener {

    @Autowired
    private PersonService personService;

    @Scheduled(fixedRate = 5000)
    public void schedule() {
        personService.listener();
    }
}
