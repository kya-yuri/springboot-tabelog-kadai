package com.example.nagoyameshi.form;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CategoryEditForm {
	@NotNull
	private Integer id;
	
	@NotBlank(message = "カテゴリ名を入力してください。")
	@Size(min = 1, max = 10, message = "カテゴリ名は10字以内で入力してください。")
	private String name;
	
	private List<Integer> stores;
}
