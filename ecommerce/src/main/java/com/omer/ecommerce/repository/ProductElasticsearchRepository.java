package com.omer.ecommerce.repository;

import com.omer.ecommerce.entity.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ProductElasticsearchRepository extends ElasticsearchRepository<Product, String> {

    List<Product> findByNameContaining(String name);

}
