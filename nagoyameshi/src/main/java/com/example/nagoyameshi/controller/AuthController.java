package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.ResetPasswordEventPublisher;
import com.example.nagoyameshi.event.SignupEventPublisher;
import com.example.nagoyameshi.form.InputEmailForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UpdatePasswordForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ログイン・会員登録・認証コントローラー
 */
@Controller
public class AuthController {
	private final UserService userService;
	private final SignupEventPublisher signupEventPublisher;
	private final VerificationTokenService verificationTokenService;
	private final UserRepository userRepository;
	private final ResetPasswordEventPublisher resetPasswordEventPublisher;
	 
	public AuthController(UserService userService, SignupEventPublisher signupEventPublisher, VerificationTokenService verificationTokenService, UserRepository userRepository, ResetPasswordEventPublisher resetPasswordEventPublisher) {
		this.userService = userService;
		this.signupEventPublisher = signupEventPublisher;
		this.verificationTokenService = verificationTokenService;
		this.userRepository = userRepository;
		this.resetPasswordEventPublisher = resetPasswordEventPublisher;
	}
	
	/**
	 * ログイン画面の表示
	 * @return	：ログインページ
	 */
	@GetMapping("/login")
    public String login() {        
        return "auth/login";
    }
	
	/**
	 * 会員登録画面の表示
	 * @param model
	 * @return	：会員登録ページ
	 */
    @GetMapping("/signup")
    public String signup(Model model) {
    	model.addAttribute("signupForm", new SignupForm());
    	return "auth/signup";
    }
    
    /**
     * 会員登録
     * @param signupForm	：会員登録フォーム
     * @param bindingResult
     * @param redirectAttributes
     * @param httpServletRequest
     * @return				：会員登録してトップページにリダイレクト
     */
    @PostMapping("/signup")
    public String signup(@ModelAttribute @Validated SignupForm signupForm,
    						BindingResult bindingResult,
    						RedirectAttributes redirectAttributes,
    						HttpServletRequest httpServletRequest) {
    	// カスタムバリデーション
    	if (userService.isEmailRegistered(signupForm.getEmail())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
    		bindingResult.addError(fieldError);
    	}
   	
    	if (!userService.isSamePassword(signupForm.getPassword(), signupForm.getPasswordConfirmation())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
    		bindingResult.addError(fieldError);
    	}
    	
    	// エラー処理
    	if (bindingResult.hasErrors()) {
    		return "auth/signup";
    	}
   	   	
    	User createdUser = userService.create(signupForm);
    	// トークンをDBから削除（万一過去のトークンが残っている可能性を考慮して）
    	verificationTokenService.delete(createdUser);
      	String requestUrl = new String(httpServletRequest.getRequestURL());
    	signupEventPublisher.publishSignupEvent(createdUser, requestUrl);
    	redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスに認証メールを送信しました。メールに記載されているリンクをクリックし、会員登録を完了してください。");
   	
    	return "redirect:/";
    }
    
    /**
     * 会員登録時のメール認証
     * @param token		：自動作成されたURLのトークン
     * @param model
     * @return			：認証ページ
     */
    @GetMapping("/signup/verify")
    public String verify(@RequestParam(name = "token") String token, Model model) {
    	VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
   	 	
    	// DBのトークンと一致する場合
    	if (verificationToken != null) {
    		User user = verificationToken.getUser();
    		userService.enableUser(user);
    		// トークンをDBから削除
    		verificationTokenService.delete(user);
   		 	String successMessage = "会員登録が完了しました。";
   		 	model.addAttribute("successMessage", successMessage);
   	 	// DBのトークンと一致しない場合
    	}else {
   	 		String errorMessage = "トークンが無効です。";
   	 		model.addAttribute("errorMessage", errorMessage);   
   	 	}
   	 
        return "auth/verify";
   	}
    
    /**
     * パスワード再設定用メールアドレス入力画面の表示
     * @param model
     * @return	：パスワード再設定用メールアドレス入力ページ
     */
    @GetMapping("/resetpass/inputemail")
    public String inputEmail(Model model) {
    	model.addAttribute("inputEmailForm", new InputEmailForm());
    	return "auth/resetpass/inputemail";
    }
    
    /**
     * パスワード再設定用メールの送信
     * @param inputEmailForm	：パスワード再設定用メールアドレス入力フォーム
     * @param bindingResult
     * @param redirectAttributes
     * @param httpServletRequest
     * @return					：パスワード再設定メールを送信してトップページにリダイレクト
     */
    @PostMapping("/resetpass")
    public String inputEmail(@ModelAttribute @Validated InputEmailForm inputEmailForm, 
    							BindingResult bindingResult, 
    							RedirectAttributes redirectAttributes, 
    							HttpServletRequest httpServletRequest) {
    	if (!userService.isEmailRegistered(inputEmailForm.getEmail()) && !bindingResult.hasErrors()) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "登録されていないメールアドレスです。");
    		bindingResult.addError(fieldError);
    	}
   	
    	if (bindingResult.hasErrors()) {
    		return "auth/resetpass/inputemail";
    	}
   	   	
    	User user = userRepository.findByEmail(inputEmailForm.getEmail());
    	// トークンをDBから削除（万一過去のトークンが残っている可能性を考慮して）
    	verificationTokenService.delete(user);
      	String requestUrl = new String(httpServletRequest.getRequestURL());
      	resetPasswordEventPublisher.publishResetPasswordEvent(user, requestUrl);
    	redirectAttributes.addFlashAttribute("successMessage", "ご入力いただいたメールアドレスにパスワード再設定用メールを送信しました。メールに記載されているリンクをクリックし、パスワードを再設定してください。");
   	
    	return "redirect:/";
    }
    
    /**
     * パスワード再設定画面の表示
     * @param model
     * @param token		：自動作成されたURLのトークン
     * @return			：パスワード再設定ページ
     */
    @GetMapping("/resetpass/verify")
    public String updatePassword(Model model, @RequestParam(name = "token") String token) {
    	VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
      	
    	// トークンが一致しない場合は認証ページに移行してメッセージ表示
    	if (verificationToken == null) {
   	 		String errorMessage = "トークンが無効です。";
   	 		model.addAttribute("errorMessage", errorMessage);
   	 		return "auth/verify";
   	 	}
   	
		User user = verificationToken.getUser();
		// パスワード再設定フォームにユーザーIDを代入
		UpdatePasswordForm updatePasswordForm = new UpdatePasswordForm(user.getId(), null, null);
		model.addAttribute("updatePasswordForm", updatePasswordForm);
		return "auth/resetpass/updatepass";
    }
    
    /**
     * パスワード更新
     * @param updatePasswordForm	：パスワード再設定フォーム
     * @param bindingResult
     * @param httpServletRequest
     * @param model
     * @return						：パスワードを更新してログインページを表示
     */
    @PostMapping("/resetpass/updatepass")
    public String updatePassword(@ModelAttribute @Validated UpdatePasswordForm updatePasswordForm,
    								BindingResult bindingResult,
    								HttpServletRequest httpServletRequest,
    								Model model) {
    	// カスタムバリデーション
    	if (!userService.isSamePassword(updatePasswordForm.getPassword(), updatePasswordForm.getPasswordConfirmation())) {
    		FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
    		bindingResult.addError(fieldError);
    	}
    	
    	// エラー処理
    	if (bindingResult.hasErrors()) {
    		return "auth/resetpass/updatepass";
    	}
   	   	
    	userService.updatePassword(updatePasswordForm);
    	User user = userRepository.getReferenceById(updatePasswordForm.getId());
    	// トークンの削除
    	verificationTokenService.delete(user);
    	model.addAttribute("successMessage", "パスワードを更新しました。");
   	
    	return "auth/login";
    }
}