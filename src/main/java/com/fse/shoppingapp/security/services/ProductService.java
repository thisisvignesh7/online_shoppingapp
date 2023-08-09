package com.fse.shoppingapp.security.services;

import com.fse.shoppingapp.exception.ProductsNotFound;
import com.fse.shoppingapp.models.Cart;
import com.fse.shoppingapp.models.Product;
import com.fse.shoppingapp.payload.response.CartResponseDTO;
import com.fse.shoppingapp.repository.CartRepository;
import com.fse.shoppingapp.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CartRepository cartRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }



    //Cart Services
    public void addToCart(Cart cart) {
        cartRepository.save(cart);
    }
    
	public Cart getAvailableProduct(String productName) {
		return cartRepository.findByProductName(productName);
	}
	
	public List<CartResponseDTO> getCartList(String loginId){
		List<CartResponseDTO> cartDTOList = new ArrayList<>();
		List<Cart> cartList = cartRepository.findByLoginId(loginId);
		for (Cart cart : cartList) {
			Product product = productRepository.findByProductName(cart.getProductName());
			cartDTOList.add(new CartResponseDTO(cart.get_id(), cart.getZipCode(), cart.getLoginId(),
					 cart.getQuantity(), product));
		}
		return cartDTOList;
	}
	
	

    public void saveProduct(Product product) {
        productRepository.save(product);
    }


    public Product getProductByName(String productName) {
    	Product productList = productRepository.findByProductName(productName);
		if (productList == null) {
			throw new ProductsNotFound("Products Not Found");
		} else {
			return productList;
		}
    
    }

    public void deleteByProductName(String productName) {
    	productRepository.deleteByProductName(productName);
    }

}
