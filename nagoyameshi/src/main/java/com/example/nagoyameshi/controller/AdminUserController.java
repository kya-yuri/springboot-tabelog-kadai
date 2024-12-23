package com.example.nagoyameshi.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;

/**
 * 【管理者用】ユーザー表示コントローラー
 */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
	private final UserRepository userRepository;        
    
    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;                
    }    
    
    /**
     * 【管理者のみ】ユーザー一覧ページの表示
     * @param keyword	：メールアドレスの検索値
     * @param pageable
     * @param model
     * @return			：ユーザー一覧ページの表示
     */
    @GetMapping
    public String index(@RequestParam(name = "keyword", required = false) String keyword, 
    					@PageableDefault(page = 0, size = 10, sort = "id", 
    					direction = Direction.ASC) Pageable pageable, 
    					Model model) {
        Page<User> userPage;
        
        // 検索処理
        if (keyword != null && !keyword.isEmpty()) {
            userPage = userRepository.findByEmailLike("%" + keyword + "%", pageable);                   
        } else {
            userPage = userRepository.findAll(pageable);
        }        
        
        model.addAttribute("userPage", userPage);        
        model.addAttribute("keyword", keyword);                
        
        return "admin/users/index";
    }
    
    /**
     * 【管理者のみ】ユーザー詳細の表示
     * @param id	：ユーザーID
     * @param model
     * @return		：ユーザー詳細ページ
     */
    @GetMapping("/{id}")
    public String show(@PathVariable(name = "id") Integer id, Model model) {
        User user = userRepository.getReferenceById(id);
        
        model.addAttribute("user", user);
        
        return "admin/users/show";
    }    
}
