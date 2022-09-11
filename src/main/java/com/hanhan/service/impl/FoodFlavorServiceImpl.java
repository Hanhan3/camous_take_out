package com.hanhan.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanhan.entity.FoodFlavor;
import com.hanhan.mapper.FoodFlavorMapper;
import com.hanhan.service.FoodFlavorService;
import org.springframework.stereotype.Service;

@Service
public class FoodFlavorServiceImpl extends ServiceImpl<FoodFlavorMapper, FoodFlavor> implements FoodFlavorService {
}
