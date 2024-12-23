package com.example.nagoyameshi.form;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 予約内容登録フォーム
 */
@Data
@AllArgsConstructor
public class ReservationRegisterForm {
	private Integer storeId;
	
	private Integer userId;
	
	private LocalDateTime reserveDateTime;
	
	private Integer numberOfPeople;
}
