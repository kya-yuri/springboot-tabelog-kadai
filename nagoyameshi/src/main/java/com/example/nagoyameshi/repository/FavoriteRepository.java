package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Favorite;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;

/**
 * 【有料会員用】お気に入り機能用リポジトリ
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {
	/**
	 * 【有料会員のみ】お気に入り一覧にユーザーがお気に入り登録した店舗を表示→お気に入り一覧ページ
	 * @param user		：ユーザー
	 * @param pageable
	 * @return
	 */
	Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
	
	/**
	 * 【有料会員のみ】ユーザーがお気に入り登録しているか確認→店舗詳細ページ
	 * @param store	：店舗情報
	 * @param user		：ユーザー
	 * @return
	 */
	Favorite findByStoreAndUser(Store store, User user);
}
