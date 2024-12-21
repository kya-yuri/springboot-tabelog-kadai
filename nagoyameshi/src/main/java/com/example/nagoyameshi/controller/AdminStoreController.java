package com.example.nagoyameshi.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
import com.example.nagoyameshi.form.StoreEditForm;
import com.example.nagoyameshi.form.StoreRegisterForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.service.StoreService;

@Controller
@RequestMapping("/admin/stores")
public class AdminStoreController {
	private final StoreRepository storeRepository;
	private final StoreService storeService;
	private final StoreCategoryRepository storeCategoryRepository;
	private final CategoryRepository categoryRepository;
     
	public AdminStoreController(StoreRepository storeRepository, StoreService storeService, StoreCategoryRepository storeCategoryRepository, CategoryRepository categoryRepository) {
		this.storeRepository = storeRepository;     
		this.storeService = storeService;
		this.storeCategoryRepository = storeCategoryRepository;
		this.categoryRepository = categoryRepository;
	}	

	/**
	 * 【管理者のみ】店舗一覧ページの表示
	 * @param model
	 * @param pageable
	 * @param keyword	：店名検索のキーワード
	 * @return			：店舗一覧ページ
	 */
	@GetMapping
	public String index(Model model,
						@PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable,
						@RequestParam(name = "keyword", required = false) String keyword) {
		Page<Store> storePage;
        
		// 検索処理
		if(keyword != null && !keyword.isEmpty()) {
			storePage = storeRepository.findByNameLike("%" + keyword + "%", pageable);
		}else{
			storePage = storeRepository.findAll(pageable);
		}
         
		model.addAttribute("storePage", storePage);             
		model.addAttribute("keyword", keyword);
         
		return "admin/stores/index";
	}  
    
	/**
	 * 【管理者のみ】店舗詳細ページの表示
	 * @param id	：店舗ID
	 * @param model
	 * @return		：店舗詳細ページ
	 */
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,
						Model model) {
		Store store = storeRepository.getReferenceById(id);
		List<Category> categories = storeCategoryRepository.findCategoriesByStoreId(id);
         
		model.addAttribute("store", store);
		model.addAttribute("strMinPrice", storeService.formatWithCommas(store.getMinPrice()));
		model.addAttribute("strMaxPrice", storeService.formatWithCommas(store.getMaxPrice()));
		model.addAttribute("store", store);
		model.addAttribute("categories", categories);
         
		return "admin/stores/show";
	}
    
	/**
	 * 【管理者のみ】店舗登録ページの表示
	 * @param model
	 * @return		：店舗登録ページ
	 */
	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryRepository.findAll();
		
		model.addAttribute("storeRegisterForm", new StoreRegisterForm());
		model.addAttribute("categories", categories);
		model.addAttribute("selectedCategoryIds", "");
		return "admin/stores/register";
	}
    
	/**
	 * 【管理者のみ】店舗の登録
	 * @param storeRegisterForm		：店舗登録フォーム
	 * @param bindingResult
	 * @param redirectAttributes
	 * @param selectedCategoryIds	：選択したカテゴリID
	 * @param model
	 * @return						：店舗を登録して店舗一覧ページにリダイレクト
	 */
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated StoreRegisterForm storeRegisterForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						@RequestParam(name = "categories", required = false) List<Integer> selectedCategoryIds,
						Model model) {
		
		// カスタムバリデーション
    	if (storeService.isNoHoliday(storeRegisterForm.getHolidays())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "holidays", "「なし」を選択した場合、他の曜日と併せて設定できません。");
    		bindingResult.addError(fieldError);
        }
		if (storeService.isPriceValid1(storeRegisterForm.getMinPrice(), storeRegisterForm.getMaxPrice())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "minPrice", "最低価格または最高価格を入力してください。");
    		bindingResult.addError(fieldError);
        }
    	
    	if (storeService.isPriceValid2(storeRegisterForm.getMinPrice(), storeRegisterForm.getMaxPrice())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "maxPrice", "最高価格は最低価格以上の値を設定してください。");
    		bindingResult.addError(fieldError);
        }
        
    	// エラー処理
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryRepository.findAll();
			model.addAttribute("categories", categories);
			model.addAttribute("selectedCategoryIds", String.join(",", selectedCategoryIds.stream().map(String::valueOf).collect(Collectors.toList())));
			
			return "admin/stores/register";
		}
         
		storeService.create(storeRegisterForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");    
         
		return "redirect:/admin/stores";
	}    
    
	/**
	 * 【管理者のみ】店舗編集ページの表示
	 * @param id	：店舗ID
	 * @param model
	 * @return		：店舗編集ページ
	 */
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
						Model model) {
		Store store = storeRepository.getReferenceById(id);
		String imageName = store.getImageName();
		List<Category> categories = categoryRepository.findAll();
		
		// カテゴリの取得
		List<Category> selectedCategories = storeCategoryRepository.findCategoriesByStoreId(id);
		String selectedCategoryIds = selectedCategories.stream().map(category -> String.valueOf(category.getId())).collect(Collectors.joining(","));
		
		// 定休日のリスト変換
		List<String> holidays = Arrays.stream(store.getHoliday().split(","))
                 					.map(String::trim) // 各要素の前後の空白を除去
                 					.collect(Collectors.toList());
		
		// 編集フォームに既存データを代入
		StoreEditForm storeEditForm = new StoreEditForm(store.getId(), 
														store.getName(), 
														null, 
														null, 
														store.getDescription(), 
														store.getOpenHour(), 
														store.getClosedHour(), 
														holidays, 
														store.getMinPrice(),
														store.getMaxPrice(),
														store.getCapacity(), 
														store.getPostalCode(), 
														store.getAddress(), 
														store.getPhoneNumber());
		
		model.addAttribute("imageName", imageName);
		model.addAttribute("storeEditForm", storeEditForm);
		model.addAttribute("categories", categories);
		model.addAttribute("selectedCategoryIds", selectedCategoryIds);
         
		return "admin/stores/edit";
	}   
    
	/**
	 * 【管理者のみ】店舗の編集
	 * @param storeEditForm			：店舗編集フォーム
	 * @param bindingResult
	 * @param redirectAttributes
	 * @param selectedCategoryIds	：選択したカテゴリID
	 * @param model
	 * @return						：店舗を更新して店舗一覧ページにリダイレクト
	 */
	@PostMapping("/{id}/update")
	public String update(@ModelAttribute @Validated StoreEditForm storeEditForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						@RequestParam(name = "categories", required = false) List<Integer> selectedCategoryIds,
						Model model) {
    	// カスタムバリデーション
		if (storeService.isNoHoliday(storeEditForm.getHolidays())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "holidays", "「なし」を選択した場合、他の曜日と併せて設定できません。");
    		bindingResult.addError(fieldError);
        }
		if (storeService.isPriceValid1(storeEditForm.getMinPrice(), storeEditForm.getMaxPrice())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "minPrice", "最低価格または最高価格を入力してください。");
    		bindingResult.addError(fieldError);
        }
    	
    	if (storeService.isPriceValid2(storeEditForm.getMinPrice(), storeEditForm.getMaxPrice())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "maxPrice", "最高価格は最低価格以上の値を設定してください。");
    		bindingResult.addError(fieldError);
        }
        	
        // エラー処理		
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryRepository.findAll();
			model.addAttribute("categories", categories);
			model.addAttribute("selectedCategoryIds", String.join(",", selectedCategoryIds.stream().map(String::valueOf).collect(Collectors.toList())));
			
			return "admin/stores/edit";
		}
    	 
		storeService.update(storeEditForm);
		redirectAttributes.addFlashAttribute("successMessage", "店舗情報を編集しました。");
    	 
		return "redirect:/admin/stores";
	}
    
	/**
	 * 【管理者のみ】店舗削除
	 * @param id		：店舗ID
	 * @param redirectAttributes
	 * @return			：店舗を削除して店舗一覧ページにリダイレクト
	 */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id,	
						RedirectAttributes redirectAttributes) {
		storeService.delete(id);
                 
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
         
		return "redirect:/admin/stores";
	}   
}