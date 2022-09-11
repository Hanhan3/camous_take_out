package com.hanhan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hanhan.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

import javax.annotation.ManagedBean;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
