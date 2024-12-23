package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;

/**
 * パスワード再設定用メール認証Listenerクラス
 */
@Component
public class ResetPasswordEventListener {
    private final VerificationTokenService verificationTokenService;    
    private final JavaMailSender javaMailSender;
    
    public ResetPasswordEventListener(VerificationTokenService verificationTokenService, JavaMailSender mailSender) {
        this.verificationTokenService = verificationTokenService;        
        this.javaMailSender = mailSender;
    }
    
    /**
     * パスワード再設定用認証メール送信
     * （ResetAPsswordSignupEventクラスから通知を受けたときに実行される処理）
     * @param resetPasswordEvent
     */
    @EventListener
    private void onResetPasswordEvent(ResetPasswordEvent resetPasswordEvent) {
        User user = resetPasswordEvent.getUser();
        String token = UUID.randomUUID().toString();
        verificationTokenService.create(user, token);
        
        String recipientAddress = user.getEmail();
        String subject = "NAGOYAMESHIパスワード再設定_メール認証";
        String confirmationUrl = resetPasswordEvent.getRequestUrl() + "/verify?token=" + token;
        String message = "以下のリンクをクリックしてパスワードを再設定してください。";
        
        SimpleMailMessage mailMessage = new SimpleMailMessage(); 
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\n" + confirmationUrl);
        javaMailSender.send(mailMessage);
    }
}
