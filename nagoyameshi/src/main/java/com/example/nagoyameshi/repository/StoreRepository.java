package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.nagoyameshi.entity.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {
	/**
	 * 店名で店舗検索→【管理者用】店舗一覧ページ
	 * @param keyword	：店名のキーワード
	 */
	Page<Store> findByNameLike(String keyword, Pageable pageable);
	
	/**
	 * 新着順、全件表示→店舗一覧ページ
	 */
	Page<Store> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	/**
	 * 新着順、店名またはカテゴリ名で検索して表示→店舗一覧ページ
	 * @param keyword	：店名またはカテゴリ名のキーワード
	 */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            WHERE (s.name LIKE %:keyword% OR c.name LIKE %:keyword%)
            ORDER BY s.createdAt DESC
    """)
    Page<Store> findStoresByKeywordOrderByCreatedAtDesc(String keyword, Pageable pageable);

	/**
	 * 新着順、カテゴリ名で特定して表示→店舗一覧ページ
	 * @param seachCategoryId	：カテゴリID
	 */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            WHERE c.id = :seachCategoryId
            ORDER BY s.createdAt DESC
    """)
    Page<Store> findStoresByCategoryIdOrderByCreatedAtDesc(Integer seachCategoryId, Pageable pageable);

    /**
     * 価格が安い順、全件表示→店舗一覧ページ
     */
    Page<Store> findAllByOrderByMinPriceAsc(Pageable pageable);
    
    /**
     * 価格が安い順、店名またはカテゴリ名で検索して表示→店舗一覧ページ
     * @param keyword	：店名またはカテゴリ名のキーワード
     */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            WHERE (:keyword IS NULL OR s.name LIKE %:keyword% OR c.name LIKE %:keyword%)
            ORDER BY s.minPrice ASC
    """)
    Page<Store> findStoresByKeywordOrderByMinPriceAsc(String keyword, Pageable pageable);
    
    /**
     * 価格が安い順、カテゴリ名で特定して表示→店舗一覧ページ
     * @param seachCategoryId	：カテゴリID
     */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            WHERE c.id = :seachCategoryId
            ORDER BY s.minPrice ASC
    """)
    Page<Store> findStoresByCategoryIdOrderByMinPriceAsc(Integer seachCategoryId, Pageable pageable);
    
    /**
     * レビューの平均スコアが高い順、全件表示→店舗一覧ページ
     */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            LEFT JOIN Review r ON s.id = r.store.id
            GROUP BY s.id
            ORDER BY AVG(r.score) DESC NULLS LAST
    """)
    Page<Store> findAllByOrderByAverageScoreDesc(Pageable pageable);
    
    /**
     * レビューの平均スコアが高い順、店名またはカテゴリ名で検索して表示→店舗一覧ページ
     * @param keyword	：店名またはカテゴリ名のキーワード
     */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            LEFT JOIN Review r ON s.id = r.store.id
            WHERE (s.name LIKE %:keyword% OR c.name LIKE %:keyword%)
            GROUP BY s.id
            ORDER BY AVG(r.score) DESC NULLS LAST
    """)
    Page<Store> findStoresByKeywordOrderByAverageScoreDesc(String keyword, Pageable pageable);
    
    /**
     * レビューの平均スコアが高い順、カテゴリ名で特定して表示→店舗一覧ページ
     * @param seachCategoryId	：カテゴリID
     */
    @Query("""
            SELECT DISTINCT s FROM Store s
            LEFT JOIN StoreCategory sc ON s.id = sc.store.id
            LEFT JOIN Category c ON sc.category.id = c.id
            LEFT JOIN Review r ON s.id = r.store.id
            WHERE c.id = :seachCategoryId
            GROUP BY s.id
            ORDER BY AVG(r.score) DESC NULLS LAST
    """)
    Page<Store> findStoresByCategoryIdOrderByAverageScoreDesc(Integer seachCategoryId, Pageable pageable);
    
    /**
     * 全店舗のレビューの平均スコアを取得→店舗一覧ページでレビューの平均値を参照するため
     */
    @Query("""
            SELECT s, AVG(r.score) AS averageScore
            FROM Store s
            LEFT JOIN Review r ON s.id = r.store.id
            GROUP BY s.id
    """)
    List<Object[]> findAllStoresWithAverageScoreRaw();
}
