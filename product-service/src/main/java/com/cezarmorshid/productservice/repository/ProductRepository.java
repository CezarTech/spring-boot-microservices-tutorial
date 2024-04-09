package com.cezarmorshid.productservice.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.cezarmorshid.productservice.model.Product;
public interface ProductRepository extends MongoRepository<Product,String> {

}
