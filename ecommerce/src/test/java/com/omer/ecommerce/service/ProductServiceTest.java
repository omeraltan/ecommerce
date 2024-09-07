package com.omer.ecommerce.service;

import com.omer.ecommerce.entity.Product;
import com.omer.ecommerce.repository.ProductElasticsearchRepository;
import com.omer.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductElasticsearchRepository productElasticsearchRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * eq("productExchange") ve eq("productRoutingKey"):
     * Bu eşleşiciler, belirli bir değer olan argümanları doğrulamak için kullanılır.
     * Bu String argümanlarının kesinlikle eşleşmesini sağlar.
     * any(Product.class):
     * Bu matcher, Product nesnesinin herhangi bir örneğini kabul eder.
     * Product nesnesi yerine Optional kullanıyorsanız, any(Optional.class) kullanmanız gerekebilir.
     */
    @Test
    public void testCreateProduct() {
        Product product = new Product();
        product.setName("Test Product");
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Simulate RabbitTemplate behavior
        doNothing().when(rabbitTemplate).convertAndSend(
            eq("productExchange"),
            eq("productRoutingKey"),
            any(Product.class) // Adjust to the actual type expected in your method
        );

        Product createdProduct = productService.saveProduct(product);
        assertNotNull(createdProduct);
        assertEquals("Test Product", createdProduct.getName());

        // Verify RabbitTemplate usage
        verify(rabbitTemplate, times(1)).convertAndSend(
            eq("productExchange"),
            eq("productRoutingKey"),
            any(Product.class) // Adjust to the actual type expected in your method
        );
    }

    @Test
    public void testCreateProduct2() {
        MockitoAnnotations.openMocks(this);

        // Arrange (Hazırlık)
        Product product = new Product();
        // Ürünü gerekli özelliklerle başlatın (örneğin, id, name vb.)
        product.setId(1L);
        product.setName("Test Product");

        // Ürün kaydedildiğinde dönecek olan kaydedilmiş ürünü belirleyin
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // Act (Uygulama)
        Product savedProduct = productService.saveProduct(product);

        // Assert (Doğrulama)
        verify(rabbitTemplate).convertAndSend(
            eq("productExchange"),
            eq("productRoutingKey"),
            eq(savedProduct)
        );

        // Ayrıca, ürünün doğru şekilde kaydedildiğini de doğrulayabilirsiniz
        verify(productRepository).save(eq(product));
    }

    @Test
    public void testGetProductById() {
        Product product = new Product();
        product.setId(1L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(1L);
        assertNotNull(foundProduct);
        assertEquals(1L, foundProduct.get().getId());
    }

    @Test
    public void testDeleteProductById() {
        doNothing().when(productRepository).deleteById(1L);
        productService.deleteProductById(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

}
