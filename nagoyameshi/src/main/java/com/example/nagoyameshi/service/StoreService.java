package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.StoreCategory;
import com.example.nagoyameshi.form.StoreEditForm;
import com.example.nagoyameshi.form.StoreRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;

/**
 * 店舗情報一般を処理するサービス
 */
@Service
public class StoreService {
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	private final StoreCategoryRepository storeCategoryRepository;
	
	public StoreService(StoreRepository storeRepository, CategoryRepository categoryRepository, StoreCategoryRepository storeCategoryRepository) {
		this.storeRepository = storeRepository;
		this.categoryRepository = categoryRepository;
		this.storeCategoryRepository = storeCategoryRepository;
	}
	
	/**
	 * 店舗登録
	 * @param storeRegisterForm	：店舗登録フォーム
	 */
	@Transactional
	public void create(StoreRegisterForm storeRegisterForm) {
		Store store = new Store();
		MultipartFile imageFile = storeRegisterForm.getImageFile();
		
		// 画像ファイルの保存
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			store.setImageName(hashedImageName);
		}
		
		// 店舗テーブルの登録
		store.setName(storeRegisterForm.getName());
		store.setDescription(storeRegisterForm.getDescription());
		store.setOpenHour(storeRegisterForm.getOpenHour());
		store.setClosedHour(storeRegisterForm.getClosedHour());
		store.setHoliday(formatHolidays(storeRegisterForm.getHolidays()));
		store.setMinPrice(storeRegisterForm.getMinPrice());
		store.setMaxPrice(storeRegisterForm.getMaxPrice());
		store.setCapacity(storeRegisterForm.getCapacity());
		store.setPostalCode(storeRegisterForm.getPostalCode());
		store.setAddress(storeRegisterForm.getAddress());
		store.setPhoneNumber(storeRegisterForm.getPhoneNumber());
		
		storeRepository.save(store);
		
		// 店舗カテゴリテーブルの登録（カテゴリ毎に一つずつ登録処理）
        for (Integer categoryId : storeRegisterForm.getCategories()) {
    		StoreCategory storeCategory = new StoreCategory();
        	Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        	storeCategory.setStore(store);
        	storeCategory.setCategory(category);
        	storeCategoryRepository.save(storeCategory);
        }	
	}
	
	/**
	 * 店舗情報更新
	 * @param storeEditForm	：店舗編集フォーム
	 */
	@Transactional
	public void update(StoreEditForm storeEditForm) {
		Store store = storeRepository.getReferenceById(storeEditForm.getId());
		MultipartFile imageFile = storeEditForm.getImageFile();
		
		// 画像ファイルの保存
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			store.setImageName(hashedImageName);
		}
		
		// 店舗テーブルの更新
		store.setName(storeEditForm.getName());
		store.setDescription(storeEditForm.getDescription());
		store.setOpenHour(storeEditForm.getOpenHour());
		store.setClosedHour(storeEditForm.getClosedHour());
		store.setHoliday(formatHolidays(storeEditForm.getHolidays()));
		store.setMinPrice(storeEditForm.getMinPrice());
		store.setMaxPrice(storeEditForm.getMaxPrice());
		store.setCapacity(storeEditForm.getCapacity());
		store.setPostalCode(storeEditForm.getPostalCode());
		store.setAddress(storeEditForm.getAddress());
		store.setPhoneNumber(storeEditForm.getPhoneNumber());
		
		storeRepository.save(store);
		
		// 店舗カテゴリテーブルから対象店舗のデータを削除して初期化
		storeCategoryRepository.deleteByStoreId(store.getId());
		// 店舗カテゴリテーブルの登録（カテゴリ毎に一つずつ登録処理）
        for (Integer categoryId : storeEditForm.getCategories()) {
    		StoreCategory storeCategory = new StoreCategory();
        	Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        	storeCategory.setStore(store);
        	storeCategory.setCategory(category);
        	storeCategoryRepository.save(storeCategory);
        }			
	}
	
	/**
	 * 店舗情報の削除（店舗カテゴリテーブルからも削除）
	 * @param storeId	：店舗ID
	 */
	@Transactional
	public void delete(Integer storeId) {
		storeCategoryRepository.deleteByStoreId(storeId);
		storeRepository.deleteById(storeId);
	}
	
	/**
	 * 画像ファイル名をハッシュ化
	 * @param fileName		：ファイル名
	 * @return				：ハッシュ化したファイル名
	 */
	private String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}
	
	/**
	 * 画像ファイルの保存
	 * @param imageFile	：画像ファイル
	 * @param filePath		：保存先パス
	 */
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 定休日のフォーマット：カンマ区切りで出力
	 * @param holidays		：定休日リスト
	 * @return				：カンマ区切りにした文字列
	 */
	private static String formatHolidays(List<String> holidays) {
			return holidays.stream().collect(Collectors.joining(", "));
	}
	
	/**
	 * 価格にカンマを付与して表示
	 * @param value		：価格数値
	 * @return				：カンマを付与した価格文字列
	 */
	public String formatWithCommas(Integer value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getInstance().format(value);
    }
	
	/**
	 * 各店舗のレビューの平均スコアを文字列で表示
	 * @return				：レビューの平均値文字列（レビューがない場合は「―」）
	 */
	public Map<Store, String> getStringStoreAverageScore() {
		List<Object[]> storeAverageScorePage = storeRepository.findAllStoresWithAverageScoreRaw();
        return storeAverageScorePage.stream().collect(Collectors.toMap(
            row -> (Store) row[0],
            row -> {
                if (row[1] == null) {
                	return "―";
                } else {
                	return String.valueOf(row[1]);
                }
            }
        ));
    }
	
	/**
	 * 各店舗のレビューの平均スコアを小数型で表示
	 * @return				：レビューの平均値小数型（レビューがない場合は「0」）
	 */
	public Map<Store, Double> getDoubleStoreAverageScore() {
		List<Object[]> storeAverageScorePage = storeRepository.findAllStoresWithAverageScoreRaw();
        return storeAverageScorePage.stream().collect(Collectors.toMap(
            row -> (Store) row[0],
            row -> {
                if (row[1] == null) {
                	return (Double) 0.0;
                } else {
                	return (Double) row[1];
                }
            }
        ));
    }
	
	// 入力時のカスタムバリデーション
	/**
	 * 「なし」を選択した場合、他の曜日は選択不可
	 * @param holidays		：定休日リスト
	 * @return				：「なし」と他の曜日が選択されている状態であればtrue
	 */
	public boolean isNoHoliday(List<String> holidays) {
		return holidays.contains("なし") && !holidays.equals(Arrays.asList("なし"));
	}
	
	/**
	 * 最低価格または最高価格のどちらか入力必須
	 * @param minPrice		：最低価格
	 * @param maxPrice		：最高価格
	 * @return				：最低価格と最高価格どちらもnullであればtrue
	 */
    public boolean isPriceValid1(Integer minPrice, Integer maxPrice) {
        return minPrice == null && maxPrice == null;
    }
    
	/**
	 * 最低価格は最高価格以下の値を入力する
	 * @param minPrice		：最低価格
	 * @param maxPrice		：最高価格
	 * @return				：最低価格と最高価格どちらも入力がある場合、最低価格が最高価格よりも大きければtrue
	 */
    public boolean isPriceValid2(Integer minPrice, Integer maxPrice) {
    	if (minPrice != null && maxPrice != null) {
    		return minPrice > maxPrice;
    	} else {
    		return false;
    	}
    }
    

}
