package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.StoreCategory;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;

/**
 * カテゴリ一般を処理するサービス
 */
@Service
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final StoreRepository storeRepository;
	private final StoreCategoryRepository storeCategoryRepository;
	
	public CategoryService(CategoryRepository categoryRepository, StoreRepository storeRepository, StoreCategoryRepository storeCategoryRepository) {
		this.categoryRepository = categoryRepository;
		this.storeRepository = storeRepository;
		this.storeCategoryRepository = storeCategoryRepository;
	}
	
	/**
	 * カテゴリー登録
	 * @param categoryRegisterForm	：カテゴリ登録フォーム
	 */
	@Transactional
	public void create(CategoryRegisterForm categoryRegisterForm) {
		// カテゴリ新規作成
		Category category = new Category();
		
		// カテゴリ登録フォームからカテゴリ名を取得してセット
		category.setName(categoryRegisterForm.getName());
		
		// カテゴリテーブルに保存
		categoryRepository.save(category);
		
		// カテゴリストアテーブルへの登録（店舗が選択されている場合、一つずつ処理）
	    for (Integer storeId : categoryRegisterForm.getStores()) {
	        // 新規登録
	    	StoreCategory storeCategory = new StoreCategory();
	    	// 店舗情報を取得（Store型で登録が必要のため）
	        Store store = storeRepository.getReferenceById(storeId);
	        // 店舗とカテゴリをセット
	        storeCategory.setStore(store);
	        storeCategory.setCategory(category);
	        // ストアカテゴリテーブルに保存
	        storeCategoryRepository.save(storeCategory);
	    }
	}

	/**
	 * カテゴリー更新
	 * @param categoryEditForm	：カテゴリ編集フォーム
	 */
	@Transactional
	public void update(CategoryEditForm categoryEditForm) {
		// 登録済みのカテゴリをIDから取得
		Category category = categoryRepository.getReferenceById(categoryEditForm.getId());
		
		// カテゴリ編集フォームからカテゴリ名を取得してセット
		category.setName(categoryEditForm.getName());
		
		// カテゴリテーブルに保存
		categoryRepository.save(category);
		
		// ストアカテゴリテーブル内の紐づけを初期化するため、対象のストアカテゴリデータを削除
		storeCategoryRepository.deleteByCategoryId(category.getId());
		// カテゴリストアテーブルへの登録（店舗が選択されている場合、一つずつ処理）
	    for (Integer storeId : categoryEditForm.getStores()) {
	        // 新規登録
	    	StoreCategory storeCategory = new StoreCategory();
	    	// 店舗情報を取得（Store型で登録が必要のため）
	        Store store = storeRepository.getReferenceById(storeId);
	        // 店舗とカテゴリをセット
	        storeCategory.setStore(store);
	        storeCategory.setCategory(category);
	        // ストアカテゴリテーブルに保存
	        storeCategoryRepository.save(storeCategory);
	    }
	}
	
	/**
	 * カテゴリー削除
	 * @param categoryId	：カテゴリID
	 */
	@Transactional
	public void delete(Integer categoryId) {
		storeCategoryRepository.deleteByCategoryId(categoryId);
		categoryRepository.deleteById(categoryId);
	}
	
	/**
	 * 各カテゴリに対応する店舗名をカンマ区切りで表示
	 * @return：カテゴリIDとそれに対応する店舗名の羅列
	 */
	public Map<Category, String> getCategoryStoreNames() {
		// 全カテゴリを取得
        List<Category> categories = categoryRepository.findAll();
        // 各カテゴリIDにマッピング
        return categories.stream().collect(Collectors.toMap(
            category -> category,
            category -> {
            	// カテゴリに対応する全店舗を取得
                List<Store> stores = storeCategoryRepository.findStoresByCategoryId(category.getId());
                if (stores.isEmpty()) {
                	// カテゴリに対応する店舗名がない場合は「―」を表示
                	return "―";
                } else {
                	// カンマ区切りで店舗名を羅列
                	return stores.stream().map(Store::getName).collect(Collectors.joining(", "));
                }
            }
        ));
    }
	
	/**
	 * 各カテゴリに対応する店舗数を表示
	 * @return：カテゴリIDとそれに対応する店舗数
	 */
	public Map<Category, Integer> getCategoryStoreCount() {
		// 全カテゴリを取得
        List<Category> categories = categoryRepository.findAll();
        // 各カテゴリIDにマッピング
        return categories.stream().collect(Collectors.toMap(
            category -> category,
            category -> {
            	// カテゴリに対応する全店舗を取得
                List<Store> stores = storeCategoryRepository.findStoresByCategoryId(category.getId());
                // 店舗数を返す
                return stores.size();
            }
        ));
    }
}
