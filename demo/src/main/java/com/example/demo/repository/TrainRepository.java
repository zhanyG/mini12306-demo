package com.example.demo.repository;

import com.example.demo.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    /** 按始发站和终点站模糊查询 */
    List<Train> findByStartStationContainingAndEndStationContaining(String start, String end);

    /** 仅按始发站模糊查询 */
    List<Train> findByStartStationContaining(String start);

    /** 仅按终点站模糊查询 */
    List<Train> findByEndStationContaining(String end);

    /** 按发车时间区间查询 */
    List<Train> findByDepartureTimeBetween(LocalDateTime start, LocalDateTime end);

    /** 按始发站和发车时间区间查询 */
    List<Train> findByStartStationContainingAndDepartureTimeBetween(
            String start, LocalDateTime departureStart, LocalDateTime departureEnd);

    /** 按终点站和发车时间区间查询 */
    List<Train> findByEndStationContainingAndDepartureTimeBetween(
            String end, LocalDateTime departureStart, LocalDateTime departureEnd);

    /** 按始发站、终点站和发车时间区间查询 */
    List<Train> findByStartStationContainingAndEndStationContainingAndDepartureTimeBetween(
            String start, String end, LocalDateTime departureStart, LocalDateTime departureEnd);
}
