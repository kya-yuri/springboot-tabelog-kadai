package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEvent;

import com.example.nagoyameshi.entity.User;

import lombok.Getter;

/**
 * パスワード再設定用メール認証Eventクラス
 */
@Getter
public class ResetPasswordEvent extends ApplicationEvent {
    private User user;
    private String requestUrl;        

    public ResetPasswordEvent(Object source, User user, String requestUrl) {
        super(source);
        
        this.user = user;        
        this.requestUrl = requestUrl;
    }
}
