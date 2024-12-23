package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.form.CategoryEditForm;
import com.example.nagoyameshi.form.CategoryRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.service.CategoryService;

/**
 * 【管理者用】カテゴリ表示・登録・編集コントローラー
 */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {
	private final CategoryRepository categoryRepository;
	private final CategoryService categoryService;
	private final StoreRepository storeRepository;
	private final StoreCategoryRepository storeCategoryRepository;
     
	public AdminCategoryController(CategoryRepository categoryRepository, CategoryService categoryService, StoreRepository storeRepository, StoreCategoryRepository storeCategoryRepository) {
		this.categoryRepository = categoryRepository;     
		this.categoryService = categoryService;
		this.storeRepository = storeRepository;
		this.storeCategoryRepository = storeCategoryRepository;
	}	
    
	/**
	 * 【管理者のみ】カテゴリー一覧ページの表示
	 * @param model
	 * @param pageable
	 * @param keyword	：検索キーワード（カテゴリ名）
	 * @return			：カテゴリー一覧ページ
	 */
	@GetMapping
	public String index(Model model,
						@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword) {
		Page<Category> categoryPage;
		// カテゴリに対応する複数店舗名の取得
        Map<Category, String> categoryStoreNamesMap = categoryService.getCategoryStoreNames();
        // カテゴリに対応する店舗数の取得
        Map<Category, Integer> categoryStoreCountMap = categoryService.getCategoryStoreCount();
        //検索処理
		if(keyword != null && !keyword.isEmpty()) {
			// 検索キーワードでカテゴリを表示
			categoryPage = categoryRepository.findByNameLike("%" + keyword + "%", pageable);
		}else{
			// 検索キーワードなしで検索された場合は全カテゴリ表示
			categoryPage = categoryRepository.findAll(pageable);
		}
         
		model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categoryStoreNamesMap", categoryStoreNamesMap);
        model.addAttribute("categoryStoreCountMap", categoryStoreCountMap);
		model.addAttribute("keyword", keyword);
         
		return "admin/categories/index";
	} 
	
	/**
	 * 【管理者のみ】カテゴリー登録画面の表示
	 * @param model
	 * @return		：カテゴリー登録ページ
	 */
	@GetMapping("/register")
	public String register(Model model) {
		// 登録したカテゴリに対応する店舗を選択させるため、プルダウン用に全店舗名を取得
		List<Store> stores = storeRepository.findAll();
		
		model.addAttribute("categoryRegisterForm", new CategoryRegisterForm());
		model.addAttribute("stores", stores);
		// プルダウンから店舗選択時、選択した店舗を表示させるための変数を初期化
		model.addAttribute("selectedStoreIds", "");	
		return "admin/categories/register";
	}
    
	/**
	 * 【管理者のみ】カテゴリー登録
	 * @param categoryRegisterForm	：カテゴリ登録フォーム
	 * @param bindingResult
	 * @param redirectAttributes
	 * @param selectedStoreIds		：プルダウンから選択した店舗のID
	 * @param model
	 * @return						：カテゴリを登録してカテゴリ一覧ページにリダイレクト
	 */
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated CategoryRegisterForm categoryRegisterForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						@RequestParam(name = "stores", required = false) List<Integer> selectedStoreIds,
						Model model) {
		// エラー処理
		if (bindingResult.hasErrors()) {
			// 登録したカテゴリに対応する店舗を選択させるため、プルダウンに全店舗名を表示 
			List<Store> stores = storeRepository.findAll();
			model.addAttribute("stores", stores);
			// プルダウンから店舗を選択していた場合、エラーで画面が更新されても選択店舗を保持して表示
			model.addAttribute("selectedStoreIds", String.join(",", selectedStoreIds.stream().map(String::valueOf).collect(Collectors.toList())));
			
			return "admin/categories/register";
		}
        
		// カテゴリ登録フォームの入力内容をDB登録
		categoryService.create(categoryRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを登録しました。");    
         
		return "redirect:/admin/categories";
	}    
    
	/**
	 * 【管理者のみ】カテゴリー編集画面の表示
	 * @param id	：編集対象のカテゴリID
	 * @param model
	 * @return		：カテゴリー編集ページ
	 */
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
						Model model) {
		// 編集対象のカテゴリ情報を取得
		Category category = categoryRepository.getReferenceById(id);
		// 編集するカテゴリに対応する店舗を選択させるため、プルダウン用に全店舗名を取得
		List<Store> stores = storeRepository.findAll();
		
		// 編集するカテゴリに紐づく店舗を選択済みにするため、取得してカンマ区切りでIDを羅列
		List<Store> selectedStores = storeCategoryRepository.findStoresByCategoryId(id);
		String selectedStoreIds = selectedStores.stream().map(store -> String.valueOf(store.getId())).collect(Collectors.joining(","));
		
		// カテゴリ編集フォームに登録データをインプット（店舗は別でインプット）
		CategoryEditForm categoryEditForm = new CategoryEditForm(category.getId(), 
																category.getName(),
																null);
         
		model.addAttribute("categoryEditForm", categoryEditForm);
		model.addAttribute("stores", stores);
		model.addAttribute("selectedStoreIds", selectedStoreIds);
         
		return "admin/categories/edit";
	}   
     
	/**
	 * 【管理者のみ】カテゴリー更新
	 * @param categoryEditForm	：カテゴリ編集フォーム
	 * @param bindingResult
	 * @param redirectAttributes
	 * @param selectedStoreIds	：プルダウンから選択した店舗のID
	 * @param model
	 * @return					：カテゴリを更新してカテゴリ一覧ページにリダイレクト
	 */
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated CategoryEditForm categoryEditForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						@RequestParam(name = "stores", required = false) List<Integer> selectedStoreIds,
						Model model) {
		// エラー処理
		if (bindingResult.hasErrors()) {
			// 登録したカテゴリに対応する店舗を選択させるため、プルダウンに全店舗名を表示 
			List<Store> stores = storeRepository.findAll();
			model.addAttribute("stores", stores);
			// プルダウンから店舗を選択していた場合、エラーで画面が更新されても選択店舗を保持して表示
			model.addAttribute("selectedStoreIds", String.join(",", selectedStoreIds.stream().map(String::valueOf).collect(Collectors.toList())));
			
			return "admin/categories/edit";
		}
    	
		// カテゴリ編集フォームの入力内容をDB登録
		categoryService.update(categoryEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを編集しました。");
    	 
		return "redirect:/admin/categories";
	}
    
	/**
	 * 【管理者のみ】カテゴリー削除
	 * @param id	：削除対象のカテゴリID
	 * @param redirectAttributes
	 * @return		：カテゴリを削除してカテゴリ一覧ページにリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,	
						RedirectAttributes redirectAttributes) {
		// カテゴリをDBから削除
		categoryService.delete(id);
		
		redirectAttributes.addFlashAttribute("successMessage", "カテゴリを削除しました。");
         
		return "redirect:/admin/categories";
	}   
}