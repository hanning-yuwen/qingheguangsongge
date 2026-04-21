package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.Alarm;
import com.qinghe.mapper.AlarmMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
@CrossOrigin
public class AlarmController {

    @Autowired
    private AlarmMapper alarmMapper;

    // 获取所有告警
    @GetMapping("/list")
    public List<Alarm> getList() {
        return alarmMapper.selectList(null);
    }

    // 添加告警
    @PostMapping("/add")
    public String addAlarm(@RequestBody Alarm alarm) {
        alarmMapper.insert(alarm);
        return "SUCCESS";
    }

    // 更新告警状态
    @PostMapping("/updateStatus")
    public String updateAlarmStatus(@RequestParam Integer id, @RequestParam String status) {
        Alarm alarm = alarmMapper.selectById(id);
        if (alarm != null) {
            alarm.setStatus(status);
            alarmMapper.updateById(alarm);
            return "SUCCESS";
        }
        return "FAIL";
    }

    // 处理告警
    @PostMapping("/process")
    public String processAlarm(@RequestParam Integer id, @RequestParam String handler, @RequestParam String resolution) {
        Alarm alarm = alarmMapper.selectById(id);
        if (alarm != null) {
            alarm.setStatus("processed");
            alarm.setHandler(handler);
            alarm.setResolution(resolution);
            alarm.setProcessingTime(java.time.LocalDateTime.now().toString());
            alarmMapper.updateById(alarm);
            return "SUCCESS";
        }
        return "FAIL";
    }

    // 获取未处理告警
    @GetMapping("/pending")
    public List<Alarm> getPendingAlarms() {
        return alarmMapper.selectList(
                new LambdaQueryWrapper<Alarm>().eq(Alarm::getStatus, "pending")
        );
    }
}
