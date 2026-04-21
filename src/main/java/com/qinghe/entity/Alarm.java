package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("alarms")
public class Alarm {
    @TableId
    private Integer id;
    private String deviceId;
    private String deviceName;
    private String alarmType;
    private String alarmLevel; // info, warning, error
    private String alarmMessage;
    private String alarmTime;
    private String status; // pending, processed, resolved
    private String handler;
    private String processingTime;
    private String resolution;
}
