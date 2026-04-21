package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.Cabin;
import com.qinghe.entity.CabinBooking;
import com.qinghe.entity.CabinSession;
import com.qinghe.entity.Order;
import com.qinghe.mapper.CabinBookingMapper;
import com.qinghe.mapper.CabinMapper;
import com.qinghe.mapper.CabinSessionMapper;
import com.qinghe.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cabin/session")
@CrossOrigin
public class CabinSessionController {

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CabinBookingMapper cabinBookingMapper;
    @Autowired
    private CabinSessionMapper cabinSessionMapper;
    @Autowired
    private CabinMapper cabinMapper;
    @Autowired
    private OrderMapper orderMapper;

    @PostMapping("/openDoor")
    public Map<String, Object> openDoor(@RequestParam Long userId, @RequestParam Long cabinId) {
        LocalDateTime now = LocalDateTime.now();

        CabinBooking booking = cabinBookingMapper.selectOne(
                new LambdaQueryWrapper<CabinBooking>()
                        .eq(CabinBooking::getUserId, userId)
                        .eq(CabinBooking::getCabinId, cabinId)
                        .eq(CabinBooking::getStatus, "booked")
                        .orderByAsc(CabinBooking::getStartTime)
                        .last("LIMIT 1")
        );
        if (booking == null) {
            return fail("无有效预约，无法开门");
        }

        LocalDateTime start = LocalDateTime.parse(booking.getStartTime(), DT_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(booking.getEndTime(), DT_FORMATTER);
        if (now.isBefore(start) || now.isAfter(end)) {
            return fail("不在预约时间段内，无法开门");
        }

        CabinSession active = cabinSessionMapper.selectOne(
                new LambdaQueryWrapper<CabinSession>()
                        .eq(CabinSession::getCabinId, cabinId)
                        .eq(CabinSession::getStatus, "active")
                        .last("LIMIT 1")
        );
        if (active != null) {
            return fail("当前舱位已在使用中");
        }

        CabinSession session = new CabinSession();
        session.setCabinId(cabinId);
        session.setOrderId(booking.getOrderId());
        session.setUserId(userId);
        session.setStartTime(now.format(DT_FORMATTER));
        session.setStatus("active");
        cabinSessionMapper.insert(session);

        Cabin cabin = cabinMapper.selectById(cabinId);
        if (cabin != null) {
            cabin.setStatus(2);
            cabinMapper.updateById(cabin);
        }

        Order order = orderMapper.selectById(booking.getOrderId());
        if (order != null) {
            order.setStatus(2);
            orderMapper.updateById(order);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("message", "开门成功");
        result.put("sessionId", session.getId());
        return result;
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "FAIL");
        result.put("message", message);
        return result;
    }
}
