package com.qwickie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
/**
 * @author Ankit Sinha
 */
public class QwickieApplication {
    public static void main(String[] args) {
        SpringApplication.run(QwickieApplication.class, args);
    }
}
