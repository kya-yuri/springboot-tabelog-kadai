package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEvent;

import com.example.nagoyameshi.entity.User;

import lombok.Getter;

/**
 * 会員登録用メール認証Eventクラス
 */
@Getter
public class SignupEvent extends ApplicationEvent {
    private User user;
    private String requestUrl;        

    public SignupEvent(Object source, User user, String requestUrl) {
        super(source);
        
        this.user = user;        
        this.requestUrl = requestUrl;
    }
}
