package com.example.nagoyameshi.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.StoreService;

/**
 * 【有料会員用】予約表示・登録コントローラー
 */
@Controller
public class ReservationController {
	private final ReservationRepository reservationRepository;   
	private final StoreRepository storeRepository;
	private final ReservationService reservationService;
	private final StoreCategoryRepository storeCategoryRepository;
	private final ReviewRepository reviewRepository;
	private final StoreService storeService;
	private final FavoriteRepository favoriteRepository;
	
	public ReservationController(ReservationRepository reservationRepository, StoreRepository storeRepository, ReservationService reservationService, StoreCategoryRepository storeCategoryRepository, ReviewRepository reviewRepository, StoreService storeService, FavoriteRepository favoriteRepository) {   
		this.reservationRepository = reservationRepository; 
		this.storeRepository = storeRepository;
		this.reservationService = reservationService;
		this.storeCategoryRepository = storeCategoryRepository;
		this.reviewRepository = reviewRepository;
		this.storeService = storeService;
		this.favoriteRepository = favoriteRepository;
	}    
	
	/**
	 * 【有料会員のみ】予約一覧の表示
	 * @param userDetailsImpl	：ログイン中のユーザー
	 * @param pageable
	 * @param model
	 * @return					：予約一覧ページ
	 */
	@GetMapping("/reservations")
	public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
						@PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable, 
						Model model) {
		User user = userDetailsImpl.getUser();
		Page<Reservation> reservationPage = reservationRepository.findByUserAndDateTimeGreaterThanOrderByCreatedAtDesc(user, LocalDateTime.now(ZoneId.of("Asia/Tokyo")), pageable);
        
		model.addAttribute("reservationPage", reservationPage);         
        
		return "reservations/index";
	}
	
	/**
	 * 【有料会員のみ】予約のキャンセル
	 * @param id	：予約ID（予約テーブルのユニークなID）
	 * @param redirectAttributes
	 * @return		：予約を削除して予約一覧ページにリダイレクト
	 */
	@PostMapping("/reservations/{id}/delete")
	public String deleteFromIndex(@PathVariable(name = "id") Integer id, 
							RedirectAttributes redirectAttributes) {        
		Store store = reservationRepository.getReferenceById(id).getStore();
		reservationRepository.deleteById(id);
                
		redirectAttributes.addFlashAttribute("successMessage", store.getName() + "の予約をキャンセルしました。");
        
		return "redirect:/reservations";
	} 
	
	/**
	 * 【有料会員のみ】店舗予約
	 * @param id					：店舗ID
	 * @param reservationInputForm	：予約入力フォーム
	 * @param bindingResult
	 * @param redirectAttributes
	 * @param model
	 * @param userDetailsImpl		：ログイン中のユーザー
	 * @return						：入力内容を予約確認ページへ渡す
	 */
	@PostMapping("/stores/{id}/reservations/input")
	public String input(@PathVariable(name = "id") Integer id,
						@ModelAttribute @Validated ReservationInputForm reservationInputForm,
						BindingResult bindingResult,
						RedirectAttributes redirectAttributes,
						Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl){   
		Store store = storeRepository.getReferenceById(id);
		Integer numberOfPeople = reservationInputForm.getNumberOfPeople();
		LocalDateTime reserveDateTime = reservationInputForm.getReserveDateTime();
		Integer capacity = store.getCapacity();
        
		// カスタムバリデーション
		if (numberOfPeople != null) {
			if (!reservationService.isWithinCapacity(numberOfPeople, capacity)) {
				FieldError fieldError = new FieldError(bindingResult.getObjectName(), "numberOfPeople", "予約人数が席数を超えています。");
				bindingResult.addError(fieldError);                
			}            
		}
		if (reserveDateTime != null) {
			if (!reservationService.checkDateTime(reserveDateTime)) {
				FieldError fieldError = new FieldError(bindingResult.getObjectName(), "reserveDateTime", "予約可能時間を過ぎています。");
				bindingResult.addError(fieldError);  
			}
		}
        
		// エラー処理
		if (bindingResult.hasErrors()) {            
			model.addAttribute("store", store);            
			model.addAttribute("errorMessage", "予約内容に不備があります。");
			
	    	// カテゴリを表示するため、店舗IDに紐づくカテゴリを取得
			List<Category> categories = storeCategoryRepository.findCategoriesByStoreId(id);
			// レビューを表示するため、店舗IDに紐づくレビューを取得
	        List<Review> newReviews = reviewRepository.findTop4ByStoreOrderByCreatedAtDesc(store);  
	        model.addAttribute("newReviews", newReviews);
			model.addAttribute("categories", categories);
			// 価格帯の数値にカンマをつけて表示
			model.addAttribute("strMinPrice", storeService.formatWithCommas(store.getMinPrice()));
			model.addAttribute("strMaxPrice", storeService.formatWithCommas(store.getMaxPrice()));
			
			// ログイン中の場合
	        if (userDetailsImpl != null) {
	            User user = userDetailsImpl.getUser();
	            // ユーザーが投稿したレビューを特定
	            Review review = reviewRepository.findByStoreAndUser(store, user);
	            // お気に入り登録をしているか特定
	            Favorite favorite = favoriteRepository.findByStoreAndUser(store, user);
	            model.addAttribute("myReview", review);  
	            model.addAttribute("favorite", favorite);
	        }
	           
			return "stores/show";
		}
        
		redirectAttributes.addFlashAttribute("reservationInputForm", reservationInputForm);           
        
		return "redirect:/stores/{id}/reservations/confirm";
	}
	
	/**
	 * 【有料会員のみ】予約確認ページの表示
	 * @param id					：店舗ID
	 * @param reservationInputForm	：予約入力フォーム
	 * @param userDetailsImpl		：ログイン中のユーザー
	 * @param model
	 * @return						：予約確認ページ
	 */
	@GetMapping("/stores/{id}/reservations/confirm")
	public String confirm(@PathVariable(name = "id") Integer id,
							@ModelAttribute ReservationInputForm reservationInputForm,
							@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,                          
							Model model){        
		Store store = storeRepository.getReferenceById(id);
		User user = userDetailsImpl.getUser(); 
        
		// 予約登録フォームに予約入力フォームの入力内容を代入
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm(store.getId(),
																						user.getId(), 
																						reservationInputForm.getReserveDateTime(),
																						reservationInputForm.getNumberOfPeople());
        
		model.addAttribute("store", store);  
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);       
        
		return "reservations/confirm";
	}    
	
	/**
	 * 予約登録
	 * @param reservationRegisterForm	：予約登録フォーム
	 * @return							：予約登録して予約一覧ページにリダイレクト
	 */
    @PostMapping("/stores/{id}/reservations/create")
    public String create(@ModelAttribute ReservationRegisterForm reservationRegisterForm) {                
        reservationService.create(reservationRegisterForm);        
        
        return "redirect:/reservations?reserved";
    }
}
