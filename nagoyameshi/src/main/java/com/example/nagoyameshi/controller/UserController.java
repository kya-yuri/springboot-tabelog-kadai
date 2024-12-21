package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;
 
@Controller
@RequestMapping("/user")
public class UserController {
	private final UserRepository userRepository;  
    private final UserService userService;
     
    public UserController(UserRepository userRepository, UserService userService) {
    	this.userRepository = userRepository; 
        this.userService = userService; 
         
    }     
    
    /**
     * 会員情報ページの表示
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param model
     * @return					：会員情報ページ
     */
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {         
    	// ログイン中のユーザーのユーザー情報を取得
    	User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
         
        model.addAttribute("user", user);
         
        return "user/index";
    }
    
    /**
     * 会員情報編集ページの表示
     * @param userDetailsImpl	：ログイン中のユーザー
     * @param model
     * @return					：会員情報編集ページ
     */
    @GetMapping("/edit")
    public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {        
    	// ログイン中のユーザーのユーザー情報を取得
    	User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        // 会員情報編集フォームにユーザー情報をセット
    	UserEditForm userEditForm = new UserEditForm(user.getId(), user.getName(), user.getFurigana(), user.getPostalCode(), user.getAddress(), user.getPhoneNumber(), user.getEmail());
         
        model.addAttribute("userEditForm", userEditForm);
         
        return "user/edit";
    }    
    
    /**
     * 会員情報の更新
     * @param userEditForm	：会員情報編集フォーム
     * @param bindingResult
     * @param redirectAttributes
     * @return				：会員情報を更新して会員情報ページにリダイレクト
     */
    @PostMapping("/update")
    public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
    	// メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
        	FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);                       
        }
        // エラー処理
        if (bindingResult.hasErrors()) {
        	return "user/edit";
        }
        
        // 会員情報編集フォームの内容をDB登録
        userService.update(userEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
         
        return "redirect:/user";
    } 
}
