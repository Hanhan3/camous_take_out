package com.hanhan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanhan.entity.Orders;

public interface OrdersService extends IService<Orders> {
    public void submit(Orders orders);
}
