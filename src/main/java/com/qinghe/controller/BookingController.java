package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.CabinBooking;
import com.qinghe.entity.Order;
import com.qinghe.mapper.CabinBookingMapper;
import com.qinghe.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/booking")
@CrossOrigin
public class BookingController {

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private CabinBookingMapper cabinBookingMapper;
    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/available")
    public Map<String, Object> getAvailableSlots(@RequestParam Long cabinId, @RequestParam String bookingDate) {
        List<CabinBooking> bookings = cabinBookingMapper.selectList(
                new LambdaQueryWrapper<CabinBooking>()
                        .eq(CabinBooking::getCabinId, cabinId)
                        .eq(CabinBooking::getBookingDate, bookingDate)
                        .ne(CabinBooking::getStatus, "cancelled")
                        .ne(CabinBooking::getStatus, "expired")
        );

        List<String> bookedRanges = new ArrayList<>();
        for (CabinBooking booking : bookings) {
            bookedRanges.add(booking.getStartTime() + " ~ " + booking.getEndTime());
        }

        List<String> availableSlots = buildAvailableSlots(bookingDate, bookings);
        Map<String, Object> result = new HashMap<>();
        result.put("cabinId", cabinId);
        result.put("bookingDate", bookingDate);
        result.put("bookedRanges", bookedRanges);
        result.put("availableSlots", availableSlots);
        return result;
    }

    @PostMapping("/create")
    public Map<String, Object> createBooking(@RequestBody Map<String, Object> payload) {
        Long userId = toLong(payload.get("userId"));
        Long cabinId = toLong(payload.get("cabinId"));
        String startTime = String.valueOf(payload.get("startTime"));
        String endTime = String.valueOf(payload.get("endTime"));

        LocalDateTime start = LocalDateTime.parse(startTime, DT_FORMATTER);
        LocalDateTime end = LocalDateTime.parse(endTime, DT_FORMATTER);
        if (!end.isAfter(start)) {
            return fail("结束时间必须晚于开始时间");
        }

        List<CabinBooking> sameCabinBookings = cabinBookingMapper.selectList(
                new LambdaQueryWrapper<CabinBooking>()
                        .eq(CabinBooking::getCabinId, cabinId)
                        .eq(CabinBooking::getBookingDate, start.toLocalDate().format(D_FORMATTER))
                        .ne(CabinBooking::getStatus, "cancelled")
                        .ne(CabinBooking::getStatus, "expired")
        );
        for (CabinBooking booking : sameCabinBookings) {
            LocalDateTime bStart = LocalDateTime.parse(booking.getStartTime(), DT_FORMATTER);
            LocalDateTime bEnd = LocalDateTime.parse(booking.getEndTime(), DT_FORMATTER);
            if (isOverlap(start, end, bStart, bEnd)) {
                return fail("该时间段已被预约");
            }
        }

        long duration = java.time.Duration.between(start, end).toMinutes();
        double fee = duration * 0.8D;

        Order order = new Order();
        order.setUserId(Math.toIntExact(userId));
        order.setCabinId(Math.toIntExact(cabinId));
        order.setStartTime(start.format(DT_FORMATTER));
        order.setDurationMin((int) duration);
        order.setFee(fee);
        order.setStatus(1);
        orderMapper.insert(order);

        CabinBooking booking = new CabinBooking();
        booking.setOrderId(order.getId().longValue());
        booking.setUserId(userId);
        booking.setCabinId(cabinId);
        booking.setBookingDate(start.toLocalDate().format(D_FORMATTER));
        booking.setStartTime(start.format(DT_FORMATTER));
        booking.setEndTime(end.format(DT_FORMATTER));
        booking.setStatus("booked");
        cabinBookingMapper.insert(booking);

        Map<String, Object> result = new HashMap<>();
        result.put("result", "SUCCESS");
        result.put("orderId", order.getId());
        result.put("bookingId", booking.getId());
        result.put("fee", fee);
        return result;
    }

    private List<String> buildAvailableSlots(String bookingDate, List<CabinBooking> bookings) {
        List<String> slots = new ArrayList<>();
        LocalDate date = LocalDate.parse(bookingDate, D_FORMATTER);
        LocalDateTime current = LocalDateTime.of(date, LocalTime.of(8, 0));
        LocalDateTime end = LocalDateTime.of(date, LocalTime.of(22, 0));
        while (current.isBefore(end)) {
            LocalDateTime next = current.plusMinutes(30);
            boolean occupied = false;
            for (CabinBooking booking : bookings) {
                LocalDateTime bStart = LocalDateTime.parse(booking.getStartTime(), DT_FORMATTER);
                LocalDateTime bEnd = LocalDateTime.parse(booking.getEndTime(), DT_FORMATTER);
                if (isOverlap(current, next, bStart, bEnd)) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                slots.add(current.toLocalTime().format(HM_FORMATTER) + "-" + next.toLocalTime().format(HM_FORMATTER));
            }
            current = next;
        }
        return slots;
    }

    private boolean isOverlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    private Map<String, Object> fail(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("result", "FAIL");
        result.put("message", message);
        return result;
    }

    private Long toLong(Object v) {
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        return Long.parseLong(String.valueOf(v));
    }
}
