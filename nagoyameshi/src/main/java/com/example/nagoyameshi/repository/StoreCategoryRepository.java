package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.StoreCategory;

public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Integer> {
	@Query("SELECT category FROM StoreCategory WHERE store.id = :storeId")
	List<Category> findCategoriesByStoreId(Integer storeId);
	
	@Query("SELECT store FROM StoreCategory WHERE category.id = :categoryId")
	List<Store> findStoresByCategoryId(Integer categoryId);
	
	@Modifying
    @Query("DELETE FROM StoreCategory WHERE store.id = :storeId")
    void deleteByStoreId(Integer storeId);
	
	@Modifying
    @Query("DELETE FROM StoreCategory WHERE category.id = :categoryId")
    void deleteByCategoryId(Integer categoryId);
}
