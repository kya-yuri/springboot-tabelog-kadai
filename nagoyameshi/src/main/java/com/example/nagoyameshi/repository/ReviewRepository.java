package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;

/**
 * レビュー機能リポジトリ
 */
public interface ReviewRepository extends JpaRepository<Review, Integer> {
	/**
	 * 最新の4つのレビューを表示→店舗詳細ページ
	 * @param store	：店舗情報
	 * @return
	 */
	List<Review> findTop4ByStoreOrderByCreatedAtDesc(Store store);
	
	/**
	 * 【有料会員用】ユーザーが投稿したレビューを特定→店舗詳細ページ、レビュー一覧ページ
	 * @param store	：店舗情報
	 * @param user		：ユーザー
	 * @return
	 */
	Review findByStoreAndUser(Store store, User user);
	
	/**
	 * 店舗のレビューを表示→レビュー一覧ページ
	 * @param store	：店舗情報
	 * @param pageable
	 * @return
	 */
	Page<Review> findByStoreOrderByCreatedAtDesc(Store store, Pageable pageable);
}
