package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;

@Controller
public class HomeController {
	private final StoreRepository storeRepository;
	private final CategoryRepository categoryRepository;
	
	public HomeController(StoreRepository storeRepository, CategoryRepository categoryRepository) {
		this.storeRepository = storeRepository;
		this.categoryRepository = categoryRepository;
	}
	
	/**
	 * トップページの表示
	 * @param model
	 * @return	：トップページ
	 */
	@GetMapping("/")
    public String index(Model model) {
		// 店舗画像を自動スクロールするため、店舗テーブルから画像ファイル名を取得
		List<String> images = storeRepository.findAll().stream().map(Store::getImageName)
											.filter(imageName -> imageName != null && !imageName.isEmpty())	// 画像ファイルがない場合を省く
											.collect(Collectors.toList());
		
		// 更新が新しい順に20件カテゴリを表示
		List<Category> categories20 = categoryRepository.findTop20ByOrderByUpdatedAtDesc();
		
		model.addAttribute("images", images);
		model.addAttribute("categories20", categories20);
		
		return "index";
	}
}
