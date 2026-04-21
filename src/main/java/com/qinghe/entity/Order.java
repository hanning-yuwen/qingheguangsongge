package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("orders")
public class Order {
    @TableId
    private Integer id;
    @TableField("user_id")
    private Integer userId;
    @TableField("cabin_id")
    private Integer cabinId;
    @TableField("start_time")
    private String startTime;
    @TableField("duration_min")
    private Integer durationMin; // 分钟
    @TableField("fee")
    private Double fee;
    @TableField("status")
    private Integer status;
}
