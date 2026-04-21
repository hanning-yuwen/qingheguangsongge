package com.qinghe.controller;

import com.qinghe.entity.Cabin;
import com.qinghe.mapper.CabinMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cabin")
@CrossOrigin // 解决前后端跨域问题
public class CabinController {

    @Autowired
    private CabinMapper cabinMapper;

    // 1. 获取所有点位状态 (用于前端列表展示)
    @GetMapping("/list")
    public List<Cabin> getList() {
        return cabinMapper.selectList(null);
    }

    // 2. 模拟预约/开门/结算 (直接通过传参改变状态)
    // status: 1-预约, 2-开门使用, 0-结束
    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Integer id, @RequestParam Integer status) {
        Cabin cabin = cabinMapper.selectById(id);
        if (cabin != null) {
            cabin.setStatus(status);
            // 如果是结束(0)，顺便模拟一下光伏发电量的微小变化
            if(status == 0) cabin.setPowerOutput(Math.random() * 20);
            cabinMapper.updateById(cabin);
            return "SUCCESS";
        }
        return "FAIL";
    }
}
