package com.example.nagoyameshi.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRegisterForm {
    @NotNull(message = "評価を選択してください。")
    private Integer score;
            
    @NotBlank(message = "コメントを入力してください。")
    private String comment;  
    
}