package com.hanhan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanhan.dto.DishDto;
import com.hanhan.dto.SetmealDto;
import com.hanhan.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐，同时需要保存套餐和菜品之间的关联
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐，同时删除套餐与菜品之间的关联
    public void deleteWithDish(List<Long> ids);

    //查询套餐，同时查询套餐与菜品之间的关联
    public SetmealDto getByIdWithDish(Long id);

    //更新套餐，同时更新套餐与菜品之间的关联
    public void updateWithDish(SetmealDto setmealDto);
}
