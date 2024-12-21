package com.example.nagoyameshi.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRegisterForm {
	// カテゴリ名
	@NotBlank(message = "カテゴリ名を入力してください。")
	@Size(min = 1, max = 10, message = "カテゴリ名は10字以内で入力してください。")
	private String name;
	
	// 店舗
	private List<Integer> stores;
}
