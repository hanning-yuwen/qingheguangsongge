package com.qinghe.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qinghe.entity.Order;
import com.qinghe.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    // 获取所有订单
    @GetMapping("/list")
    public List<Order> getList() {
        return orderMapper.selectList(null);
    }

    // 添加订单
    @PostMapping("/add")
    public String addOrder(@RequestBody Order order) {
        // 防止前端传入 id 导致主键冲突，新增统一走数据库自增
        order.setId(null);
        orderMapper.insert(order);
        return "SUCCESS";
    }

    // 更新订单
    @PostMapping("/update")
    public String updateOrder(@RequestBody Order order) {
        orderMapper.updateById(order);
        return "SUCCESS";
    }

    // 删除订单
    @PostMapping("/delete")
    public String deleteOrder(@RequestParam Integer id) {
        orderMapper.deleteById(id);
        return "SUCCESS";
    }

    // 获取用户订单
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Integer userId) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId)
        );
    }

    // 获取订单详情
    @GetMapping("/detail/{id}")
    public Order getOrderDetail(@PathVariable Integer id) {
        return orderMapper.selectById(id);
    }
}
