package com.qinghe.controller;

import com.qinghe.entity.Device;
import com.qinghe.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device")
@CrossOrigin
public class DeviceController {

    @Autowired
    private DeviceMapper deviceMapper;

    // 获取所有设备
    @GetMapping("/list")
    public List<Device> getList() {
        return deviceMapper.selectList(null);
    }

    // 添加设备
    @PostMapping("/add")
    public String addDevice(@RequestBody Device device) {
        deviceMapper.insert(device);
        return "SUCCESS";
    }

    // 更新设备
    @PostMapping("/update")
    public String updateDevice(@RequestBody Device device) {
        deviceMapper.updateById(device);
        return "SUCCESS";
    }

    // 删除设备
    @PostMapping("/delete")
    public String deleteDevice(@RequestParam Integer id) {
        deviceMapper.deleteById(id);
        return "SUCCESS";
    }

    // 获取设备状态
    @GetMapping("/status")
    public List<Device> getDeviceStatus() {
        return deviceMapper.selectList(null);
    }
}
