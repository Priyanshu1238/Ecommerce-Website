package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Product;
import com.ecom.repository.ProductRepository;
import com.ecom.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService{

	@Autowired
	private ProductRepository pr;
	@Override
	public Product saveProduct(Product product) {
		// TODO Auto-generated method stub
		
		return pr.save(product);
	}
	@Override
	public List<Product> getAllProduct() {
		// TODO Auto-generated method stub
		
		return pr.findAll();
	}
	@Override
	public Boolean deleteProduct(Integer id) {
		// TODO Auto-generated method stub
		Product product=pr.findById(id).orElse(null);
		
		if(!ObjectUtils.isEmpty(product))
		{
			pr.delete(product);
			return true;
			
		}
		
		return false;
	}
	@Override
	public Product getProductById(Integer id) {
		// TODO Auto-generated method stub
		Product product=pr.findById(id).orElse(null);
		return product;
	}
	@Override
	public Product updateProduct(Product product,MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		
		Product dbProduct=getProductById(product.getId());
		String imagename=file.isEmpty()?dbProduct.getImage():file.getOriginalFilename();
		
		dbProduct.setTitle(product.getTitle());
		dbProduct.setDescription(product.getDescription());
		dbProduct.setCategory(product.getCategory());
		dbProduct.setPrice(product.getPrice());
		dbProduct.setStock(product.getStock());
		
		dbProduct.setIsActive(product.getIsActive());
		dbProduct.setImage(imagename);
		
		//discount
		
		dbProduct.setDiscount(product.getDiscount());
		
		Double d=product.getPrice()*(product.getDiscount()/100.0);
		dbProduct.setDiscountPrice(product.getPrice()-d);
		
		
		
		Product updateProduct=pr.save(dbProduct);
		
		if(!ObjectUtils.isEmpty(updateProduct))
		{
			
			if(!file.isEmpty())
			{
				
				
File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				
				
				
				
				
			}
			return product;
			
		}
		
		
		
		return null;
	}
	@Override
	public List<Product> getAllActiveProduct(String category) {
		// TODO Auto-generated method stub
		List<Product> p=null;
		//List<Product>q=null;
		if(ObjectUtils.isEmpty(category)) {
		p=pr.findByIsActiveTrue();
		}
		else {
			p=pr.findByCategory(category);
			
		}
		return p;
	}
	@Override
	public List<Product> searchProduct(String ch) {
		// TODO Auto-generated method stub
		
		return pr.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);
		
	
	}
	@Override
	public Page<Product> getAllActiveProductPagination(Integer pageNo, Integer pageSize,String category) {
		// TODO Auto-generated method stub
		
		Pageable pageable=PageRequest.of(pageNo, pageSize);

		Page<Product> pageProduct=null;
		
		//List<Product> p=null;
		//List<Product>q=null;
		if(ObjectUtils.isEmpty(category)) {
			pageProduct=pr.findByIsActiveTrue(pageable);
		}
		else {
			pageProduct=pr.findByCategory(pageable,category);
			
		}
		
	
		
		return pageProduct;
	}
	@Override
	public Page<Product> searchProductPagination(Integer pageNo, Integer pageSize, String ch) {
		// TODO Auto-generated method stub
		
		Pageable pageable=PageRequest.of(pageNo , pageSize);
		
		return pr.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch,ch,pageable);
		
		
	}
	@Override
	public Page<Product> getAllProductPagination(Integer pageNo, Integer pageSize) {
		// TODO Auto-generated method stub
		
		Pageable pageable=PageRequest.of(pageNo, pageSize);
		
		return pr.findAll(pageable);
	}

}
