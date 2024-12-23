package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Role;

/**
 * ロール管理リポジトリ
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
	/**
	 * ロールを特定→会員登録・更新時のロール登録（User Service）に使用
	 * @param name
	 * @return
	 */
	public Role findByName(String name);
}
