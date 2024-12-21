package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	List<Review> findTop4ByStoreOrderByCreatedAtDesc(Store store);
	
	Review findByStoreAndUser(Store store, User user);
	
	Page<Review> findByStoreOrderByCreatedAtDesc(Store store, Pageable pageable);
}
