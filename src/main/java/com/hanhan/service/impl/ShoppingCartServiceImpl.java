package com.hanhan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanhan.entity.ShoppingCart;
import com.hanhan.mapper.ShoppingCartMapper;
import com.hanhan.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
