package com.example.nagoyameshi.form;

import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 店舗情報編集フォーム
 */
@Data
@AllArgsConstructor
public class StoreEditForm {
	@NotNull
	private Integer id;
	
	@NotBlank(message = "店名を入力してください。")
	private String name;
	
	@NotEmpty(message = "カテゴリを設定してください。")
	private List<Integer> categories;
         
	private MultipartFile imageFile;
     
	@NotBlank(message = "説明を入力してください。")
	private String description;   
    
	@NotNull(message = "営業開始時間を入力してください。")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime openHour;
     
	@NotNull(message = "営業終了時間を入力してください。")
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime closedHour;
	
	@NotEmpty(message = "定休日を設定してください。")
	private List<String> holidays;
	
	// private String otherHoliday;
	
	@PositiveOrZero(message = "最低価格は1以上で入力してください。")
	private Integer minPrice;
	
	@PositiveOrZero(message = "最高価格は1以上で入力してください。")
	private Integer maxPrice;
    
	@NotNull(message = "席数を入力してください。")
	@Min(value = 1, message = "席数は1人以上に設定してください。")
	private Integer capacity;     
     
	@NotBlank(message = "郵便番号を入力してください。")
	private String postalCode;
     
	@NotBlank(message = "住所を入力してください。")
	private String address;
     
	@NotBlank(message = "電話番号を入力してください。")
	private String phoneNumber;
}
