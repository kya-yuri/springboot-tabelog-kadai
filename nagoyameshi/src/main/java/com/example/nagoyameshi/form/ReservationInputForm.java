package com.example.nagoyameshi.form;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 予約内容入力フォーム
 */
@Data
public class ReservationInputForm {
	@NotNull(message = "予約日時を設定してください。")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") // 変換パターンを指定
	private LocalDateTime reserveDateTime;
	
	@NotNull(message = "予約人数を入力してください。")
	@Min(value = 1, message = "予約人数は1人以上に設定してください。")
	private Integer numberOfPeople;
}
