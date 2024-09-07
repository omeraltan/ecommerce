package com.omer.ecommerce.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    @Bean
    public Queue productQueue() {
        return new Queue("product-queue", true);
    }

    @Bean
    public Queue productDeleteQueue() {
        return new Queue("product-delete-queue", true);
    }

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange("product-exchange");
    }

    @Bean
    public Binding productBinding(Queue productQueue, DirectExchange productExchange) {
        return BindingBuilder.bind(productQueue).to(productExchange).with("product-routing-key");
    }

    @Bean
    public Binding productDeleteBinding(Queue productDeleteQueue, DirectExchange productExchange) {
        return BindingBuilder.bind(productDeleteQueue).to(productExchange).with("product-delete-routing-key");
    }
}
