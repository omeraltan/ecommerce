package com.omer.ecommerce.messaging;

import com.omer.ecommerce.entity.Product;
import com.omer.ecommerce.repository.ProductElasticsearchRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @Autowired
    private ProductElasticsearchRepository productElasticsearchRepository;

    @RabbitListener(queues = "product-queue")
    public void handleProductMessage(Product product) {
        // Index the product in Elasticsearch
        productElasticsearchRepository.save(product);
    }

    @RabbitListener(queues = "product-delete-queue")
    public void handleProductDeleteMessage(Long productId) {
        try {
            // Elasticsearch'ten ürünü sil
            productElasticsearchRepository.deleteById(String.valueOf(productId));
            System.out.println("Product deleted from Elasticsearch: " + productId);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error while deleting product from Elasticsearch: " + productId);
        }
    }

}
