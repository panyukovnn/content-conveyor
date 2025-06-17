package ru.panyukovnn.contentconveyor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import ru.panyukovnn.contentconveyor.client.feign.TgChatsCollectorFeignClient;

@SpringBootApplication
@EnableFeignClients(clients = TgChatsCollectorFeignClient.class)
public class ContentConveyorApp {

    public static void main(String[] args) {
        SpringApplication.run(ContentConveyorApp.class, args);
    }
}
