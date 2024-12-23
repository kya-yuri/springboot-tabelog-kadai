package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.User;

/**
 * ユーザー情報を管理するリポジトリ
 */
public interface UserRepository extends JpaRepository<User, Integer> {
	/**
	 * メールアドレスでユーザーを特定→会員登録、パスワード再設定時のユーザー確認
	 * @param email	：メールアドレス
	 * @return
	 */
	User findByEmail(String email);
	
	/**
	 * メールアドレスでユーザーを検索→ユーザー一覧ページ
	 * @param email	：メールアドレス
	 * @param pageable
	 * @return
	 */
	Page<User> findByEmailLike(String email, Pageable pageable);
}
