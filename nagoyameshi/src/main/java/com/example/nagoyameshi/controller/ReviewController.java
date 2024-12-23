package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReviewService;

/**
 * レビュー表示・【有料会員用】レビュー登録・編集コントローラー
 */
@Controller
@RequestMapping("/stores/{storeId}/reviews")
public class ReviewController {
	private final StoreRepository storeRepository;
	private final ReviewRepository reviewRepository;
	private final ReviewService reviewService;
    
	public ReviewController(StoreRepository storeRepository, ReviewRepository reviewRepository, ReviewService reviewService) {
		this.storeRepository = storeRepository;
		this.reviewRepository = reviewRepository;
		this.reviewService = reviewService;
	}     
    
	/**
	 * レビュー一覧の表示
	 * @param storeId	：店舗ID
	 * @param pageable
	 * @param model
	 * @return			：レビュー一覧ページ
	 */
	@GetMapping
	public String index(@PathVariable(name = "storeId") Integer storeId,
						@PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable,
						Model model) {
		Store store = storeRepository.getReferenceById(storeId);
		Page<Review> reviewPage = reviewRepository.findByStoreOrderByCreatedAtDesc(store, pageable);
        
		model.addAttribute("store", store);   
		model.addAttribute("reviewPage", reviewPage);
        
		return "reviews/index";
	} 
    
	/**
	 * 【有料会員のみ】レビュー登録ページの表示
	 * @param storeId	：店舗ID
	 * @param model
	 * @return			：レビュー登録ページ
	 */
	@GetMapping("/register")
	public String register(@PathVariable(name = "storeId") Integer storeId, Model model) {
		Store store = storeRepository.getReferenceById(storeId);
        
		model.addAttribute("store", store);  
		model.addAttribute("reviewRegisterForm", new ReviewRegisterForm());
		return "reviews/register";
	}
	
	/**
	 * 【有料会員のみ】レビュー登録    
	 * @param storeId				：店舗ID
	 * @param userDetailsImpl		：ログイン中のユーザー
	 * @param reviewRegisterForm	：レビュー登録フォーム
	 * @param bindingResult
	 * @param model
	 * @return						：レビュー登録してレビュー一覧ページにリダイレクト
	 */
	@PostMapping("/register/create")
	public String create(@PathVariable(name = "storeId") Integer storeId,
							@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,     
							@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, 
							BindingResult bindingResult,
							Model model) {       
		Store store = storeRepository.getReferenceById(storeId);
		User user = userDetailsImpl.getUser(); 
        
		// エラー処理
		if (bindingResult.hasErrors()) {
			model.addAttribute("store", store);
			return "reviews/register";
		}
        
		reviewService.create(store, user, reviewRegisterForm);
        
		return "redirect:/stores/{storeId}/reviews";
	}  
    
	/**
	 * 【有料会員のみ】レビュー編集ページの表示
	 * @param storeId		：店舗ID
	 * @param reviewId		：レビューID（レビューテーブルのユニークなID）
	 * @param model
	 * @return				：レビュー編集ページ
	 */
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "storeId") Integer storeId, @PathVariable(name = "reviewId") Integer reviewId, Model model) {       
		Store store = storeRepository.getReferenceById(storeId);
		Review review = reviewRepository.getReferenceById(reviewId);
		// レビュー編集ページに既存データを代入
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getId(), review.getScore(), review.getComment());
        
		model.addAttribute("store", store);
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);
        
		return "reviews/edit";
	}
    
	/**
	 * 【有料会員のみ】レビュー更新
	 * @param storeId			：店舗ID
	 * @param reviewId			：レビューID（レビューテーブルのユニークなID）
	 * @param reviewEditForm	：レビュー編集フォーム
	 * @param bindingResult
	 * @param model
	 * @return					：レビュー更新してレビュー一覧ページにリダイレクト
	 */
	@PostMapping("/{reviewId}/update")
	public String update(@PathVariable(name = "storeId") Integer storeId,
							@PathVariable(name = "reviewId") Integer reviewId,
							@ModelAttribute @Validated ReviewEditForm reviewEditForm, 
							BindingResult bindingResult,
							Model model) {       
		Store store = storeRepository.getReferenceById(storeId);
		Review review = reviewRepository.getReferenceById(reviewId);
        
		if (bindingResult.hasErrors()) {
			model.addAttribute("store", store);
			model.addAttribute("review", review);
			return "reviews/edit";
		}
               
		reviewService.update(reviewEditForm);
        
		return "redirect:/stores/{storeId}/reviews";
	}
    
	/**
	 * 【有料会員のみ】レビュー削除
	 * @param reviewId		：レビューID（レビューテーブルのユニークなID）
	 * @param redirectAttributes
	 * @return				：レビュー削除して店舗詳細ページにリダイレクト
	 */
	@PostMapping("/{reviewId}/delete")
	public String delete(@PathVariable(name = "reviewId") Integer reviewId, RedirectAttributes redirectAttributes) {        
		reviewRepository.deleteById(reviewId);
                
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
        
		return "redirect:/stores/{storeId}";
	}   

}
