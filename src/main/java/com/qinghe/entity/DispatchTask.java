package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dispatch_task")
public class DispatchTask {
    @TableId
    private Long id;
    @TableField("task_type")
    private String taskType;
    @TableField("task_date")
    private String taskDate;
    @TableField("planned_time")
    private String plannedTime;
    private String status;
    @TableField("params_json")
    private String paramsJson;
    @TableField("result_json")
    private String resultJson;
    @TableField("error_message")
    private String errorMessage;
    @TableField("started_at")
    private String startedAt;
    @TableField("finished_at")
    private String finishedAt;
}
