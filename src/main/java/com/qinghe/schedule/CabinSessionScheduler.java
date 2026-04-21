package com.qinghe.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.Cabin;
import com.qinghe.entity.CabinBooking;
import com.qinghe.entity.CabinSession;
import com.qinghe.entity.DispatchTask;
import com.qinghe.entity.Order;
import com.qinghe.mapper.CabinBookingMapper;
import com.qinghe.mapper.CabinMapper;
import com.qinghe.mapper.CabinSessionMapper;
import com.qinghe.mapper.DispatchTaskMapper;
import com.qinghe.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CabinSessionScheduler {

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private CabinSessionMapper cabinSessionMapper;
    @Autowired
    private CabinBookingMapper cabinBookingMapper;
    @Autowired
    private CabinMapper cabinMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DispatchTaskMapper dispatchTaskMapper;

    @Scheduled(fixedDelay = 60000)
    public void closeTimeoutSessions() {
        DispatchTask task = new DispatchTask();
        task.setTaskType("timeout_check");
        task.setTaskDate(LocalDate.now().format(D_FORMATTER));
        task.setPlannedTime(LocalDateTime.now().format(DT_FORMATTER));
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now().format(DT_FORMATTER));
        dispatchTaskMapper.insert(task);

        int closedCount = 0;
        try {
            List<CabinSession> activeSessions = cabinSessionMapper.selectList(
                    new LambdaQueryWrapper<CabinSession>().eq(CabinSession::getStatus, "active")
            );
            LocalDateTime now = LocalDateTime.now();
            for (CabinSession session : activeSessions) {
                CabinBooking booking = cabinBookingMapper.selectOne(
                        new LambdaQueryWrapper<CabinBooking>()
                                .eq(CabinBooking::getOrderId, session.getOrderId())
                                .last("LIMIT 1")
                );
                if (booking == null) {
                    continue;
                }
                LocalDateTime bookingEnd = LocalDateTime.parse(booking.getEndTime(), DT_FORMATTER);
                if (now.isAfter(bookingEnd)) {
                    int durationMin = (int) Duration.between(
                            LocalDateTime.parse(session.getStartTime(), DT_FORMATTER),
                            now
                    ).toMinutes();
                    double fee = durationMin * 0.8D;

                    session.setEndTime(now.format(DT_FORMATTER));
                    session.setDurationMin(durationMin);
                    session.setFee(fee);
                    session.setStatus("finished");
                    session.setCloseReason("timeout");
                    cabinSessionMapper.updateById(session);

                    Order order = orderMapper.selectById(session.getOrderId());
                    if (order != null) {
                        order.setDurationMin(durationMin);
                        order.setFee(fee);
                        order.setStatus(3);
                        orderMapper.updateById(order);
                    }

                    Cabin cabin = cabinMapper.selectById(session.getCabinId());
                    if (cabin != null) {
                        cabin.setStatus(0);
                        cabinMapper.updateById(cabin);
                    }
                    closedCount++;
                }
            }
            task.setStatus("success");
            task.setResultJson("{\"closedCount\":" + closedCount + "}");
        } catch (Exception e) {
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
        }
        task.setFinishedAt(LocalDateTime.now().format(DT_FORMATTER));
        dispatchTaskMapper.updateById(task);
    }
}
