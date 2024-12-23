package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.StoreCategory;

/**
 * 店舗とカテゴリの紐づけを管理するリポジトリ
 */
public interface StoreCategoryRepository extends JpaRepository<StoreCategory, Integer> {
	
	/**
	 * 店舗情報にカテゴリを表示→店舗詳細ページ、店舗情報編集ページ
	 * @param storeId		：店舗ID
	 * @return
	 */
	@Query("SELECT category FROM StoreCategory WHERE store.id = :storeId")
	List<Category> findCategoriesByStoreId(Integer storeId);
	
	/**
	 * カテゴリ一覧に店舗名、店舗数を表示→カテゴリ一覧ページ
	 * @param categoryId
	 * @return
	 */
	@Query("SELECT store FROM StoreCategory WHERE category.id = :categoryId")
	List<Store> findStoresByCategoryId(Integer categoryId);
	
	/**
	 * 店舗に紐づくカテゴリを削除
	 * @param storeId		：店舗ID
	 */
	@Modifying
    @Query("DELETE FROM StoreCategory WHERE store.id = :storeId")
    void deleteByStoreId(Integer storeId);

	/**
	 * カテゴリに紐づく店舗を削除
	 * @param categoryId		：カテゴリID
	 */
	@Modifying
    @Query("DELETE FROM StoreCategory WHERE category.id = :categoryId")
    void deleteByCategoryId(Integer categoryId);
}
