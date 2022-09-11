package com.hanhan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanhan.dto.DishDto;
import com.hanhan.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增商品，同时插入商品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //查询商品及其对应的口味数据
    public DishDto getByIdWithFlavor(Long id);

    //修改商品信息及其对应的口味
    public void updateWithFlavor(DishDto dishDto);
}
