package com.example.nagoyameshi.service;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Store;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.StoreRepository;
import com.example.nagoyameshi.repository.UserRepository;

/**
 * 予約機能を処理するサービス
 */
@Service
public class ReservationService {
	private final ReservationRepository reservationRepository;  
	private final StoreRepository storeRepository;  
	private final UserRepository userRepository;  
    
	public ReservationService(ReservationRepository reservationRepository, StoreRepository storeRepository, UserRepository userRepository) {
		this.reservationRepository = reservationRepository;  
		this.storeRepository = storeRepository;  
		this.userRepository = userRepository;  
	}    
    
	/**
	 * 予約登録
	 * @param reservationRegisterForm	：予約登録フォーム
	 */
	@Transactional
	public void create(ReservationRegisterForm reservationRegisterForm) { 
		Reservation reservation = new Reservation();
		Store store = storeRepository.getReferenceById(reservationRegisterForm.getStoreId());
		User user = userRepository.getReferenceById(reservationRegisterForm.getUserId());      
                
		reservation.setStore(store);
		reservation.setUser(user);
		reservation.setDateTime(reservationRegisterForm.getReserveDateTime());
		reservation.setNumberOfPeople(reservationRegisterForm.getNumberOfPeople());
        
		reservationRepository.save(reservation);
	}    
	
	// 入力時のカスタムバリデーション
	/**
	 * 予約人数が席数以内かどうか
	 * @param numberOfPeople	：予約人数
	 * @param capacity			：席数
	 * @return					：予約人数が席数以下であればtrue
	 */
    public boolean isWithinCapacity(Integer numberOfPeople, Integer capacity) {
        return numberOfPeople <= capacity;
    }
    
    public boolean checkDateTime(LocalDateTime reserveDateTime) {
    	return reserveDateTime.isAfter(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
    }
}
