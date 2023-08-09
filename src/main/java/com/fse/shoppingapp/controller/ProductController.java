package com.fse.shoppingapp.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.fse.shoppingapp.exception.ProductsNotFound;
import com.fse.shoppingapp.exception.UserNotFound;
import com.fse.shoppingapp.models.Cart;
import com.fse.shoppingapp.models.Product;
import com.fse.shoppingapp.models.User;
import com.fse.shoppingapp.payload.request.LoginRequest;
import com.fse.shoppingapp.payload.response.CartResponseDTO;
import com.fse.shoppingapp.repository.ProductRepository;
import com.fse.shoppingapp.repository.UserRepository;
import com.fse.shoppingapp.security.services.ProductService;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1.0/shopping")
@OpenAPIDefinition(info = @Info(title = "Shopping  Application API", description = "This API provides endpoints for managing Products."))
@Slf4j
public class ProductController {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private ProductRepository productRepository;
	


	@PutMapping("/{loginId}/forgot")
	@Operation(summary = "reset password")
	public ResponseEntity<String> changePassword(@RequestBody LoginRequest loginRequest, @PathVariable String loginId) {
		log.debug("forgot password endopoint accessed by " + loginRequest.getLoginId());
		if (loginId.equals(loginRequest.getLoginId())) {
			Optional<User> user1 = userRepository.findByLoginId(loginId);
			User availableUser = user1.get();
			User updatedUser = new User(loginId, availableUser.getFirstName(), availableUser.getLastName(),
					availableUser.getEmail(), availableUser.getContactNumber(),
					passwordEncoder.encode(loginRequest.getPassword()));
			updatedUser.set_id(availableUser.get_id());
			updatedUser.setRoles(availableUser.getRoles());
			userRepository.save(updatedUser);
			log.debug(loginRequest.getLoginId() + " has password changed successfully");
			return new ResponseEntity<>("Users password changed successfully", HttpStatus.OK);

		} else {
			log.debug("Enter Correct LoginId");
			throw new UserNotFound("User Not Available");

		}

	}

	@GetMapping("/all")
	@Operation(summary = "search all Products")
	public ResponseEntity<List<Product>> getAllProducts() {
		log.debug("here u can access all the available products");
		List<Product> productList = productService.getAllProducts();
		if (productList.isEmpty()) {
			log.debug("currently no products are available");
			throw new ProductsNotFound("No pproducts are available");
		} else {
			log.debug("listed the available products");
			return new ResponseEntity<>(productList, HttpStatus.FOUND);
		}
	}

	@GetMapping("/products/search/{productName}")
	@Operation(summary = "search product by product name")
	public Product getProductByName(@PathVariable String productName) {
		log.debug("here search a Product by its name");
		Product product = productService.getProductByName(productName);
		return product;
		
	}

	@PostMapping("/{productName}/add")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "Add Product")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> addProducts(@RequestBody Product product, @PathVariable String productName) {
		if (productName.equals(product.getProductName())) {
			Product productDetails = productRepository.findByProductName(productName);
			if (productDetails == null) {
				productService.saveProduct(product);
				return new ResponseEntity<>("\"Added new Product\"", HttpStatus.OK);
			} else {
				Product updateProduct = new Product(productDetails.get_id(), productDetails.getProductName(),
						productDetails.getProductDescription(), productDetails.getPrice(), productDetails.getFeatures(),
						productDetails.getQuantity(), productDetails.getProductStatus());
				updateProduct.set_id(productDetails.get_id());
				updateProduct.setQuantity(productDetails.getQuantity() + 1);
				productService.saveProduct(updateProduct);
				return new ResponseEntity<>("\"Product Already Present!! Updated Quantity\"", HttpStatus.OK);
			}

		}else {
			return new ResponseEntity<>("Enter Correct ProductName!!!! ", HttpStatus.BAD_REQUEST);
			
		}

	}

	@PutMapping("/{productName}/update/{id}")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "Update a ProductStatus(Admin Only)")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> updateProductStatus(@PathVariable String productName, @PathVariable ObjectId id) {

		Product product = productService.getProductByName(productName);
		if (product == null) {
			throw new ProductsNotFound("Products not found: " + productName);
		}
		if (product.getQuantity() == 0) {
			product.setProductStatus("OUT OF STOCK’");
		} else {
			product.setProductStatus("HURRY UP TO PURCHASE’");
		}
		productService.saveProduct(product);
		return new ResponseEntity<>("Ticket status updated successfully", HttpStatus.OK);

	}

	@DeleteMapping("/{productName}/delete/{id}")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "delete a Product(Admin Only)")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> deleteProduct(@PathVariable String productName, @PathVariable ObjectId id) {
		Product availableProducts = productService.getProductByName(productName);
		if (availableProducts == null) {
			throw new ProductsNotFound("Products not found: " + productName);
		} else {
			productService.deleteByProductName(productName);
			return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
		}

	}
	
	@PostMapping("/addToCart")
	@SecurityRequirement(name = "Bearer Authentication")
	@Operation(summary = "Add Product To Cart")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<String> addToCart(@RequestBody Cart product) {
		Product AvProduct = productService.getProductByName(product.getProductName());
		if(AvProduct != null && AvProduct.getQuantity() > 0) {
			Cart cartItem = productService.getAvailableProduct(product.getProductName());
			if(cartItem == null) {
				productService.addToCart(product);
				return new ResponseEntity<>("Product Added to Cart successfully", HttpStatus.OK);
			}else {
				Cart updateCartProduct = new Cart(cartItem.get_id(),cartItem.getLoginId(),
						cartItem.getProduct_id(),cartItem.getProductName(),
						cartItem.getQuantity(),cartItem.getZipCode());
				updateCartProduct.set_id(cartItem.get_id());
				updateCartProduct.setQuantity(cartItem.getQuantity() + product.getQuantity());
				productService.addToCart(updateCartProduct);
				return new ResponseEntity<>("Product Already Present Updated quantity", HttpStatus.OK);		
			}
			
		}else{	
			return new ResponseEntity<>("Product Not Available", HttpStatus.OK);	
		}
	}
	
	@GetMapping("/getCart/{loginId}")
	@SecurityRequirement(name = "Bearer Authentication")
	@PreAuthorize("hasRole('USER')")
	@Operation(summary = "GetCart Items For Specific User")
	public ResponseEntity<List<CartResponseDTO>> getCartByUser(@PathVariable String loginId) {
		List<CartResponseDTO> cartList = productService.getCartList(loginId);
		return new ResponseEntity<>(cartList, HttpStatus.OK);
	}

}
