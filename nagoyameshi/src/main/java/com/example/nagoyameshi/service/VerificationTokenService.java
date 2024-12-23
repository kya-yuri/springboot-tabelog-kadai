package com.example.nagoyameshi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

/**
 * 認証機能を処理するサービス
 */
@Service
public class VerificationTokenService {
	private final VerificationTokenRepository verificationTokenRepository;
	
	public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
		this.verificationTokenRepository = verificationTokenRepository;
	}
	
	/**
	 * 認証用トークンの作成（DBへの登録）
	 * @param user		：ユーザー
	 * @param token	：トークン
	 */
	@Transactional
	public void create(User user, String token) {
		VerificationToken verificationToken = new VerificationToken();
		
		verificationToken.setUser(user);
		verificationToken.setToken(token);
		
		verificationTokenRepository.save(verificationToken);
	}
	
	/**
	 * 認証用トークンの削除
	 * @param user		：ユーザー
	 */
	@Transactional
	public void delete(User user) {
		verificationTokenRepository.deleteByUserId(user.getId());
	}
	
	/**
	 * トークンの取得
	 * @param token	：トークン
	 * @return
	 */
	public VerificationToken getVerificationToken(String token) {
		return verificationTokenRepository.findByToken(token);
	}
}

