package com.example.nagoyameshi.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;

@Component
public class ResetPasswordEventPublisher {
     private final ApplicationEventPublisher applicationEventPublisher;
     
     public ResetPasswordEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;                
     }
     
     public void publishResetPasswordEvent(User user, String requestUrl) {
         applicationEventPublisher.publishEvent(new ResetPasswordEvent(this, user, requestUrl));
     }
}
