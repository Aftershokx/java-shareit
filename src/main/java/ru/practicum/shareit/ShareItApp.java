package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication
@EntityScan(basePackages = "ru.practicum")
public class ShareItApp {

    public static void main(String[] args){
        SpringApplication.run(ShareItApp.class, args);
    }

}
