package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("energy")
public class Energy {
    @TableId
    private Integer id;
    private String deviceId;
    private String deviceName;
    private Double powerConsumption;
    private Double powerGeneration;
    private String timestamp;
    private String date;
    private String time;
}
