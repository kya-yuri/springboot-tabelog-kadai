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
	
	@Transactional
	public void create(StoreRegisterForm storeRegisterForm) {
		Store store = new Store();
		MultipartFile imageFile = storeRegisterForm.getImageFile();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			store.setImageName(hashedImageName);
		}
		
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
		
        for (Integer categoryId : storeRegisterForm.getCategories()) {
    		StoreCategory storeCategory = new StoreCategory();
        	Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        	storeCategory.setStore(store);
        	storeCategory.setCategory(category);
        	storeCategoryRepository.save(storeCategory);
        }	
	}
	
	@Transactional
	public void update(StoreEditForm storeEditForm) {
		Store store = storeRepository.getReferenceById(storeEditForm.getId());
		MultipartFile imageFile = storeEditForm.getImageFile();
		
		if (!imageFile.isEmpty()) {
			String imageName = imageFile.getOriginalFilename();
			String hashedImageName = generateNewFileName(imageName);
			Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
			copyImageFile(imageFile, filePath);
			store.setImageName(hashedImageName);
		}
		
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
		
		storeCategoryRepository.deleteByStoreId(store.getId());
        for (Integer categoryId : storeEditForm.getCategories()) {
    		StoreCategory storeCategory = new StoreCategory();
        	Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        	storeCategory.setStore(store);
        	storeCategory.setCategory(category);
        	storeCategoryRepository.save(storeCategory);
        }			
	}
	
	@Transactional
	public void delete(Integer storeId) {
		storeCategoryRepository.deleteByStoreId(storeId);
		storeRepository.deleteById(storeId);
	}

	private String generateNewFileName(String fileName) {
		String[] fileNames = fileName.split("\\.");
		for (int i = 0; i < fileNames.length - 1; i++) {
			fileNames[i] = UUID.randomUUID().toString();
		}
		String hashedFileName = String.join(".", fileNames);
		return hashedFileName;
	}
	
	public void copyImageFile(MultipartFile imageFile, Path filePath) {
		try {
			Files.copy(imageFile.getInputStream(), filePath);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 定休日のフォーマット：カンマ区切りで出力
	private static String formatHolidays(List<String> holidays) {
			return holidays.stream().collect(Collectors.joining(", "));
	}
	
	/*
	// 定休日の曜日とその他休日の間の空白を作成
	private static String createHolidaysBetweenBlank(List<String> holidays, String otherHoliday){
		if (holidays != null && otherHoliday !=null) {
			return " ";
		} else {
			return "";
		}
	}
	*/
	
	// 価格にカンマを付与して表示
	public String formatWithCommas(Integer value) {
        if (value == null) {
            return "";
        }
        return NumberFormat.getInstance().format(value);
    }
	
	// 入力時のカスタムバリデーション
	// : 定休日("「なし」を選択した場合、他の曜日と併せて設定できません。")
	public boolean isNoHoliday(List<String> holidays) {
		return holidays.contains("なし") && !holidays.equals(Arrays.asList("なし"));
	}
	
	// ：価格帯("最低価格または最高価格を入力してください。"）
    public boolean isPriceValid1(Integer minPrice, Integer maxPrice) {
        return minPrice == null && maxPrice == null;
    }
    
	// ：価格帯("最高価格は最低価格以上の値を設定してください。"）
    public boolean isPriceValid2(Integer minPrice, Integer maxPrice) {
    	if (minPrice != null && maxPrice != null) {
    		return minPrice > maxPrice;
    	} else {
    		return false;
    	}
    }
    
	// 各店舗のレビューの平均スコアを文字列で表示
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
	
	// 各店舗のレビューの平均スコアを小数型で表示
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
}
