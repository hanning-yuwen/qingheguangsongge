package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.Energy;
import com.qinghe.mapper.EnergyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/energy")
@CrossOrigin
public class EnergyController {

    @Autowired
    private EnergyMapper energyMapper;

    // 获取所有能耗数据
    @GetMapping("/list")
    public List<Energy> getList() {
        return energyMapper.selectList(null);
    }

    // 添加能耗数据
    @PostMapping("/add")
    public String addEnergy(@RequestBody Energy energy) {
        energyMapper.insert(energy);
        return "SUCCESS";
    }

    // 获取设备能耗
    @GetMapping("/device/{deviceId}")
    public List<Energy> getDeviceEnergy(@PathVariable String deviceId) {
        return energyMapper.selectList(
                new LambdaQueryWrapper<Energy>().eq(Energy::getDeviceId, deviceId)
        );
    }

    // 获取能耗统计
    @GetMapping("/stats")
    public Map<String, Double> getEnergyStats() {
        List<Energy> all = energyMapper.selectList(null);
        double totalConsumption = 0D;
        double totalGeneration = 0D;
        for (Energy energy : all) {
            totalConsumption += energy.getPowerConsumption() == null ? 0D : energy.getPowerConsumption();
            totalGeneration += energy.getPowerGeneration() == null ? 0D : energy.getPowerGeneration();
        }
        Map<String, Double> result = new HashMap<>();
        result.put("totalConsumption", totalConsumption);
        result.put("totalGeneration", totalGeneration);
        return result;
    }
}
