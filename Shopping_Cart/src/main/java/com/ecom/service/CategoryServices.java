package com.ecom.service;

import java.util.List;

import com.ecom.model.Category;

public interface CategoryServices {

		public Category savecategory(Category category);
		
		public boolean existCategory(String name);
		
		
		public List<Category> getAllCategory();
		
		public Boolean deleteCategory(int id);
		
		public Category getCategoryById(int id);
		
		public List<Category> getAllActiveCategory();
}
