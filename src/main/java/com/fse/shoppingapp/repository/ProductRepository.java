package com.fse.shoppingapp.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.fse.shoppingapp.models.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product,String> {
	
	@Query("{$or:[{productName:{$regex:?0, $options:'i'}},{productName:{$regex:'^?0', $options:'i'}}]}")
    Product findByProductName(String productName);


    void deleteByProductName(String productName);
}

