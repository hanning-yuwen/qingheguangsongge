package com.qinghe.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users")
public class User {
    @TableId
    private Integer id;
    private String username;
    private String password;
    private String role; // admin, user
    private String name;
    private String phone;
}
