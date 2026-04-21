package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cabin_session")
public class CabinSession {
    @TableId
    private Long id;
    @TableField("cabin_id")
    private Long cabinId;
    @TableField("order_id")
    private Long orderId;
    @TableField("user_id")
    private Long userId;
    @TableField("start_time")
    private String startTime;
    @TableField("end_time")
    private String endTime;
    @TableField("duration_min")
    private Integer durationMin;
    private Double fee;
    private String status;
    @TableField("close_reason")
    private String closeReason;
}
