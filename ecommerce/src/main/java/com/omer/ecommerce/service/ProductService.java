package com.omer.ecommerce.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.omer.ecommerce.entity.Product;
import com.omer.ecommerce.repository.ProductRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    // Veritabanına ürün kaydetme
    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);

        // RabbitMQ'ya mesaj gönder
        rabbitTemplate.convertAndSend("productExchange", "productRoutingKey", savedProduct);

        return savedProduct;
    }

    // Veritabanından ve Redis Cache'den ürün getirme

    /**
     * Redis Cache: Ürünler getProductById metodu aracılığıyla Redis ile cache'lenir.
     * Cache, ürün silindiğinde (deleteProduct) otomatik olarak temizlenir.
     */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Ürünleri listeleme
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Ürünü veritabanından silme ve RabbitMQ'ya silme mesajı gönderme
    @CacheEvict(value = "products", key = "#id")
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);

        // RabbitMQ'ya silme mesajı gönder
        rabbitTemplate.convertAndSend("productExchange", "productDeleteRoutingKey", id);
    }

    // Elasticsearch'te ürün arama
    /**
     * Elasticsearch Client ile Arama:
     * searchProducts metodu,
     * Elasticsearch üzerinde name ve description alanlarında arama yapar ve sonuçları döndürür.
     */
    public List<Product> searchProducts(String keyword) throws IOException {
        // Elasticsearch üzerinde arama yap
        SearchRequest request = SearchRequest.of(s -> s
            .index("products")
            .query(q -> q
                .multiMatch(m -> m
                    .fields("name", "description")
                    .query(keyword)
                )
            )
        );

        SearchResponse<Product> response = elasticsearchClient.search(request, Product.class);

        return response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
    }
}
