package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Category;
import com.ecom.repository.CategoryRepository;
import com.ecom.service.CategoryServices;

@Service
public class CategoryServicesImpl implements CategoryServices {

	@Autowired
	private CategoryRepository cr;
	
	@Override
	public Category savecategory(Category category) {
		// TODO Auto-generated method stub
		return cr.save(category);
	}

	@Override
	public List<Category> getAllCategory() {
		// TODO Auto-generated method stub
		return cr.findAll();
	}

	@Override
	public boolean existCategory(String name) {
		// TODO Auto-generated method stub
		return cr.existsByName(name);
	}

	@Override
	public Boolean deleteCategory(int id) {
		// TODO Auto-generated method stub
		Category category=cr.findById(id).orElse(null);
		if(!ObjectUtils.isEmpty(category)) {
			cr.delete(category);
			return true;
		}
		
		return false;
	}

	@Override
	public Category getCategoryById(int id) {
		// TODO Auto-generated method stub
		
		Category c=cr.findById(id).orElse(null);
		return c;
	}

	@Override
	public List<Category> getAllActiveCategory() {
		// TODO Auto-generated method stub
		
		List<Category> c=cr.findByIsActiveTrue();
		return c;
	}

	@Override
	public Page<Category> getAllCategoryPagination(Integer pageNo,Integer pageSize) {
		// TODO Auto-generated method 
		
		Pageable pageable=PageRequest.of(pageNo,pageSize);
		return cr.findAll(pageable);

		
		
		
	
	}

}
