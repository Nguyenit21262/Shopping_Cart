package com.ecom.service;

import java.util.List;
import com.ecom.model.Category;

public interface CategoryService {

	 Category saveCategory(Category category);
	
	 List<Category> getAllCategory();
	 
	 Boolean existsCategory(String name);
	 
	 public Boolean deleteCategory(int id);
	 
	 public Category getCategoryById(int id);
	 
	 public List<Category> getAllActiveCategory();
}
