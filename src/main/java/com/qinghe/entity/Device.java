package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("devices")
public class Device {
    @TableId
    private Integer id;
    private String name;
    private String type; // cabin, sensor, etc.
    private String status; // normal, warning, error
    private String location;
    private String ipAddress;
    private String macAddress;
    private String lastOnline;
    private String description;
}
