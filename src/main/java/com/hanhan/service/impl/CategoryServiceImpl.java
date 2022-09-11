package com.hanhan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanhan.common.CustomException;
import com.hanhan.entity.Category;
import com.hanhan.entity.Dish;
import com.hanhan.entity.Setmeal;
import com.hanhan.mapper.CategoryMapper;
import com.hanhan.service.CategoryService;
import com.hanhan.service.DishService;
import com.hanhan.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 删除分类
     * @param ids
     */
    @Override
    public void remove(Long ids) {
        LambdaQueryWrapper<Dish> dishlqw = new LambdaQueryWrapper<>();
        //添加查询条件，根据分类id进行查询
        dishlqw.eq(Dish::getCategoryId,ids);
        int count1 = dishService.count(dishlqw);
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        if(count1 >0){
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmeallqw = new LambdaQueryWrapper<>();
        setmeallqw.eq(Setmeal::getCategoryId,ids);
        //添加查询条件，根据分类id进行查询
        int count2  = setmealService.count(setmeallqw);
        if(count2 >0){
            //已经关联套餐，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        super.removeById(ids);
    }
}
