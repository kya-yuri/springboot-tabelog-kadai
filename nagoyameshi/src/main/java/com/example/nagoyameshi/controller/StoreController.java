package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationInputForm;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.StoreCategoryRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StoreService;

/**
 * 店舗表示コントローラー
 */
@Controller
@RequestMapping("/stores")
public class StoreController {
    private final StoreRepository storeRepository;
    private final StoreService storeService;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final StoreCategoryRepository storeCategoryRepository;
    private final CategoryRepository categoryRepository;
    
    public StoreController(StoreRepository storeRepository, StoreService storeService, ReviewRepository reviewRepository, FavoriteRepository favoriteRepository, StoreCategoryRepository storeCategoryRepository, CategoryRepository categoryRepository) {
        this.storeRepository = storeRepository;
        this.storeService = storeService;        		
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.storeCategoryRepository = storeCategoryRepository;
        this.categoryRepository = categoryRepository;
    }     
    
    /**
     * 店舗一覧ページの表示（店名またはカテゴリ名のLike検索）
     * @param keyword	：店名またはカテゴリ名の検索キーワード
     * @param order		：新規順（NULL）/価格が安い順/評価が高い順
     * @param pageable
     * @param model
     * @return			：店舗一覧ページ
     */
    @GetMapping
    public String seachKeyword(@RequestParam(name = "keyword", required = false) String keyword,
		                        @RequestParam(name = "order", required = false) String order,
		                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
		                        Model model){
        Page<Store> storePage;
        // カテゴリ検索用プルダウン用に全カテゴリを取得
        List<Category> categories = categoryRepository.findAll();
        // 数値表示用に、店舗に対応するレビューの平均値（文字列型）を取得（レビューなしの場合は「―」と表示するため）
        Map<Store, String> storeAverageScoreMapString = storeService.getStringStoreAverageScore();
        // ★表示用に、店舗に対応するレビューの平均値（数値型）を取得
        Map<Store, Double> storeAverageScoreMapDouble = storeService.getDoubleStoreAverageScore();
        
        // 検索処理
        if (keyword != null && !keyword.isEmpty()) {
        	// 価格が安い順にキーワードで検索して表示
            if (order != null && order.equals("minPriceAsc")) {
                storePage = storeRepository.findStoresByKeywordOrderByMinPriceAsc(keyword, pageable);
            } else
            // 評価が高い順にキーワードで検索して表示
            if (order != null && order.equals("scoreDesc")) {
                storePage = storeRepository.findStoresByKeywordOrderByAverageScoreDesc(keyword, pageable);
            // 新規順にキーワードで検索して表示
            } else {
            	storePage = storeRepository.findStoresByKeywordOrderByCreatedAtDesc(keyword, pageable);
            }
        } else {
        	// 価格が安い順に全件表示
            if (order != null && order.equals("minPriceAsc")) {
                storePage = storeRepository.findAllByOrderByMinPriceAsc(pageable);
            } else
            // 評価が高い順に全件表示
            if (order != null && order.equals("scoreDesc")) {
                storePage = storeRepository.findAllByOrderByAverageScoreDesc(pageable);
            // 新規順に全件表示
            } else {
            	storePage = storeRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        }                
        
        model.addAttribute("storePage", storePage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("order", order);
        model.addAttribute("storeService", storeService);	// 価格帯をカンマ区切りで表示するメソッドを使用するため
        model.addAttribute("storeAverageScoreMapString", storeAverageScoreMapString);
        model.addAttribute("storeAverageScoreMapDouble", storeAverageScoreMapDouble);
        
        return "stores/index";
    }
    
    /**
     * 店舗一覧ページの表示（カテゴリの検索）
     * @param seachCategoryId	：カテゴリID
     * @param order				：新規順（NULL）/価格が安い順/評価が高い順
     * @param pageable
     * @param model
     * @return					：店舗一覧ページ
     */
    @GetMapping("/seachCategory")
    public String seachCategory(@RequestParam(name = "seachCategoryId", required = false) Integer seachCategoryId,
                        @RequestParam(name = "order", required = false) String order,
                        @PageableDefault(page = 0, size = 10, sort = "id", direction = Direction.ASC) Pageable pageable,
                        Model model){
        Page<Store> storePage;
        // カテゴリ検索用プルダウン用に全カテゴリを取得
        List<Category> categories = categoryRepository.findAll();
        // 数値表示用に、店舗に対応するレビューの平均値（文字列型）を取得（レビューなしの場合は「―」と表示するため）
        Map<Store, String> storeAverageScoreMapString = storeService.getStringStoreAverageScore();
        // ★表示用に、店舗に対応するレビューの平均値（数値型）を取得
        Map<Store, Double> storeAverageScoreMapDouble = storeService.getDoubleStoreAverageScore();
        
        // 検索処理（カテゴリ未選択での検索は不可）
        // 価格が安い順にキーワードで検索して表示
        if (order != null && order.equals("minPriceAsc")) {
            storePage = storeRepository.findStoresByCategoryIdOrderByAverageScoreDesc(seachCategoryId, pageable);
        } else
        // 評価が高い順にキーワードで検索して表示
        if (order != null && order.equals("scoreDesc")) {
            storePage = storeRepository.findStoresByCategoryIdOrderByMinPriceAsc(seachCategoryId, pageable);
        // 新規順にキーワードで検索して表示
        } else {
        	storePage = storeRepository.findStoresByCategoryIdOrderByCreatedAtDesc(seachCategoryId, pageable);
        }      
        
        model.addAttribute("storePage", storePage);
        model.addAttribute("categories", categories);
        model.addAttribute("seachCategoryId", seachCategoryId);
        model.addAttribute("order", order);
        model.addAttribute("storeService", storeService);	// 価格帯をカンマ区切りで表示するメソッドを使用するため
        model.addAttribute("storeAverageScoreMapString", storeAverageScoreMapString);
        model.addAttribute("storeAverageScoreMapDouble", storeAverageScoreMapDouble);
        
        return "stores/index";
    }
    
    /**
     * 店舗詳細ページの表示
     * @param id				：店舗ID
     * @param model
     * @param userDetailsImpl	：ログイン中のユーザー
     * @return					：店舗詳細ページ
     */
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id, 
    					Model model, 
    					@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        // 店舗IDから店舗情報を取得
    	Store store = storeRepository.getReferenceById(id);
    	// カテゴリを表示するため、店舗IDに紐づくカテゴリを取得
		List<Category> categories = storeCategoryRepository.findCategoriesByStoreId(id);
		// レビューを表示するため、店舗IDに紐づくレビューを取得
        List<Review> newReviews = reviewRepository.findTop4ByStoreOrderByCreatedAtDesc(store);  
              
        model.addAttribute("store", store);   
        model.addAttribute("reservationInputForm", new ReservationInputForm());
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
}
