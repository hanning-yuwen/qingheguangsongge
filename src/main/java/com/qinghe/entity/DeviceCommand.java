package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("device_command")
public class DeviceCommand {
    @TableId
    private Long id;
    @TableField("device_id")
    private Long deviceId;
    @TableField("command_type")
    private String commandType;
    @TableField("command_payload")
    private String commandPayload;
    private String status;
    @TableField("result_message")
    private String resultMessage;
    @TableField("issued_by")
    private Long issuedBy;
    @TableField("issued_at")
    private String issuedAt;
    @TableField("executed_at")
    private String executedAt;
}
