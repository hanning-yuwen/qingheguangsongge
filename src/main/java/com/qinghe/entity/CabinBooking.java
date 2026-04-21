package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cabin_booking")
public class CabinBooking {
    @TableId
    private Long id;
    @TableField("order_id")
    private Long orderId;
    @TableField("user_id")
    private Long userId;
    @TableField("cabin_id")
    private Long cabinId;
    @TableField("booking_date")
    private String bookingDate;
    @TableField("start_time")
    private String startTime;
    @TableField("end_time")
    private String endTime;
    private String status;
}
