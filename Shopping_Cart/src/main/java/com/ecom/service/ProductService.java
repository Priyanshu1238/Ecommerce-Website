package com.ecom.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;

public interface ProductService {

	public Product saveProduct(Product product);
	
	public List<Product> getAllProduct();
	
	public Boolean deleteProduct(Integer id);
	
	public Product getProductById(Integer id);
	
	public Product updateProduct(Product product,MultipartFile file) throws IOException;
	
	public List<Product> getAllActiveProduct(String category);
	
	public List<Product> searchProduct(String ch);
	
	public Page<Product> getAllActiveProductPagination(Integer pageNo,Integer pageSize,String category);

}
