package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.User;
import com.qinghe.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserMapper userMapper;

    // 登录
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, user.getUsername())
                        .eq(User::getPassword, user.getPassword())
                        .last("LIMIT 1")
        );
        if (dbUser != null) {
            return "SUCCESS:" + dbUser.getRole();
        }
        return "FAIL";
    }

    // 获取所有用户
    @GetMapping("/list")
    public List<User> getList() {
        return userMapper.selectList(null);
    }

    // 添加用户
    @PostMapping("/add")
    public String addUser(@RequestBody User user) {
        // 防止前端传入 id 导致主键冲突，新增统一走数据库自增
        user.setId(null);
        userMapper.insert(user);
        return "SUCCESS";
    }

    // 更新用户
    @PostMapping("/update")
    public String updateUser(@RequestBody User user) {
        userMapper.updateById(user);
        return "SUCCESS";
    }

    // 删除用户
    @PostMapping("/delete")
    public String deleteUser(@RequestParam Integer id) {
        userMapper.deleteById(id);
        return "SUCCESS";
    }
}
