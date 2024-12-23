package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.User;

/**
 * 【有料会員用】予約機能用リポジトリ
 */
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	/**
	 * 現在日時よりも後の予約のみ表示→予約一覧ページ
	 * @param user			：ユーザー
	 * @param nowDateTime	：現在日時
	 * @param pageable
	 * @return
	 */
	Page<Reservation> findByUserAndDateTimeGreaterThanOrderByCreatedAtDesc(User user, LocalDateTime nowDateTime, Pageable pageable);
}
