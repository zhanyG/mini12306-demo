package com.example.demo.repository;

import com.example.demo.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    /** 查询某用户的所有常用乘客 */
    List<Passenger> findByUserId(Long userId);
}
