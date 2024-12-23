package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.VerificationToken;

/**
 * 認証機能リポジトリ
 */
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
	/**
	 * トークンが有効かどうか確認
	 * @param token	：トークン
	 * @return
	 */
	VerificationToken findByToken(String token);
	/**
	 * ユーザーのトークンを削除
	 * @param userId	：ユーザーID
	 */
	void deleteByUserId(Integer userId);
}
