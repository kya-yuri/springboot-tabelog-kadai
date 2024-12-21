package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	/**
	 * カテゴリ名でカテゴリを検索→【管理者用】カテゴリ一覧ページ
	 * @param keyword	：カテゴリ名のキーワード
	 */
	Page<Category> findByNameLike(String keyword, Pageable pageable);
	
	/**
	 * 更新が新しい順に20件カテゴリを表示→opページ
	 * @return
	 */
	List<Category> findTop20ByOrderByUpdatedAtDesc();
}
