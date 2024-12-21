package com.example.nagoyameshi.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	Page<Reservation> findByUserAndDateTimeGreaterThanOrderByCreatedAtDesc(User user, LocalDateTime nowDateTime, Pageable pageable);
}
