package org.axon;

import org.axon.config.AxonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ AxonConfig.class })
public class AxonsampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(AxonsampleApplication.class, args);
        System.out.println("Hello world!");
    }
}