package com.example.nagoyameshi.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UpdatePasswordForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

/**
 * ユーザー一般を処理するサービス
 */
@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	/**
	 * 新規会員登録
	 * @param signupForm	：会員情報登録フォーム
	 */
	@Transactional
	public User create(SignupForm signupForm) {
		// ユーザーの新規作成
		User user = new User();
		// ロールは「無料会員」で作成
		Role role = roleRepository.findByName("ROLE_GENERAL");
		
		// 会員情報登録フォームの内容をセット
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
        user.setPostalCode(signupForm.getPostalCode());
        user.setAddress(signupForm.getAddress());
        user.setPhoneNumber(signupForm.getPhoneNumber());
        user.setEmail(signupForm.getEmail());
        user.setPassword(passwordEncoder.encode(signupForm.getPassword()));	// パスワードはハッシュ化
        user.setRole(role);	// 「無料会員」をセット
        user.setEnabled(false);	// ユーザーが有効かどうかは初期無効化      
        
        // ユーザーテーブルに保存
        return userRepository.save(user);
	}
	
	/**
	 * 会員情報の更新
	 * @param userEditForm	：会員情報編集フォーム
	 */
	@Transactional
    public void update(UserEditForm userEditForm) {
		// 登録済みのユーザー情報をIDから取得
        User user = userRepository.getReferenceById(userEditForm.getId());
        
        // 会員情報編集フォームの内容をセット
        user.setName(userEditForm.getName());
        user.setFurigana(userEditForm.getFurigana());
        user.setPostalCode(userEditForm.getPostalCode());
        user.setAddress(userEditForm.getAddress());
        user.setPhoneNumber(userEditForm.getPhoneNumber());
        user.setEmail(userEditForm.getEmail());      
        
        // ユーザーテーブルに保存
        userRepository.save(user);
    }
	
	/**
	 * パスワード更新
	 * @param updatePasswordForm	：パスワード再設定フォーム
	 */
	@Transactional
    public void updatePassword(UpdatePasswordForm updatePasswordForm) {
		// 登録済みのユーザー情報をIDから取得
        User user = userRepository.getReferenceById(updatePasswordForm.getId());
        
        // パスワード再設定フォームに入力されたパスワードをハッシュ化して登録
        user.setPassword(passwordEncoder.encode(updatePasswordForm.getPassword()));  

        // ユーザーテーブルに保存
        userRepository.save(user);
    }
	
	/**
	 * 対象ユーザーのStripe顧客IDを登録
	 * @param user	：対象ユーザー
	 * @param stripeCustomerId	：Stripeの顧客ID
	 */
    @Transactional
    public void saveStripeCustomerId(User user, String stripeCustomerId) {
        user.setStripeCustomerId(stripeCustomerId);
        userRepository.save(user);
    }
    
    /**
     * 対象ユーザーのロールを更新
     * @param user	：対象ユーザー
     * @param roleName	：ロール名
     */
    @Transactional
    public void updateRole(User user, String roleName) {
        Role role = roleRepository.findByName(roleName);
        user.setRole(role);
        userRepository.save(user);
    }
    
    /**
     * 認証情報のロールを更新する（ロールが変更されてもログイン状態のままロールを更新する）
     * @param newRole	：更新するロール名
     */
    public void refreshAuthenticationByRole(String newRole) {
        // 現在の認証情報を取得する
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();

        // 新しい認証情報を作成する
        List<SimpleGrantedAuthority> simpleGrantedAuthorities = new ArrayList<>();
        simpleGrantedAuthorities.add(new SimpleGrantedAuthority(newRole));
        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(currentAuthentication.getPrincipal(), currentAuthentication.getCredentials(), simpleGrantedAuthorities);

        // 認証情報を更新する
        SecurityContextHolder.getContext().setAuthentication(newAuthentication);
    }
	
    /**
     * ユーザーを有効にする
     * @param user	：対象のユーザー
     */
    @Transactional
    public void enableUser(User user) {
    	// ユーザーを有効にするかどうかをtrueにしてユーザーテーブルへ保存
        user.setEnabled(true); 
        userRepository.save(user);
    } 
    
    // カスタムバリデーション
    /**
     * メールアドレスが登録済みかどうかをチェックする
     * @param email	：メールアドレス
     * @return		：登録済みであればtrueを返す
     */
    public boolean isEmailRegistered(String email) {
        User user = userRepository.findByEmail(email);  
        return user != null;
    }
    
    /**
     * パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする
     * @param password	：パスワード
     * @param passwordConfirmation	：パスワード（確認用）
     * @return	：一致すればtrueを返す
     */
    public boolean isSamePassword(String password, String passwordConfirmation) {
        return password.equals(passwordConfirmation);
    } 

    /**
     * メールアドレスが変更されたかどうかをチェックする
     * @param userEditForm	：会員情報編集フォーム
     * @return	：会員情報編集フォームに新たに入力されたメールアドレスと現在のメールアドレスがイコールでなければtrueを返す
     */
    public boolean isEmailChanged(UserEditForm userEditForm) {
        User currentUser = userRepository.getReferenceById(userEditForm.getId());
        return !userEditForm.getEmail().equals(currentUser.getEmail());      
    }  
}
