package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("cabins")
public class Cabin {
    @TableId
    private Integer id;
    private String name;
    private Integer status; // 0:空闲, 1:预约, 2:使用中
    private Double powerOutput;
    private String location;
}
