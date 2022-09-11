package com.hanhan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanhan.common.CustomException;
import com.hanhan.dto.DishDto;
import com.hanhan.dto.SetmealDto;
import com.hanhan.entity.Category;
import com.hanhan.entity.Setmeal;
import com.hanhan.entity.SetmealDish;
import com.hanhan.mapper.SetmealMapper;
import com.hanhan.service.CategoryService;
import com.hanhan.service.SetmealDishService;
import com.hanhan.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐，同时保存套餐和菜品之间的关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal表，执行Insert操作
        this.save(setmealDto);

        //保存套餐和菜品的关联信息，操作setmeal_dish表，执行Insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {
        //select * from setmeal where ids in(*) and status = 1;
        //判断是否为启售状态，若是，则不能删除
        LambdaQueryWrapper<Setmeal> setmeallqw = new LambdaQueryWrapper<>();
        setmeallqw.in(Setmeal::getId,ids);
        setmeallqw.eq(Setmeal::getStatus,1);
        int count = this.count(setmeallqw);
        if(count >0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如可以删除，从setmeal表中删除
        this.removeByIds(ids);
        //从setmealDIsh表中删除
        //delete from setmeal_dish where setmealid in(*);
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lqw);
    }

    /**
     * 查询套餐及其以与菜品的关系
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> setmealDishList = setmealDishService.list(lqw);

        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(Category::getId,setmeal.getCategoryId());
        Category categoryServiceOne = categoryService.getOne(categoryLambdaQueryWrapper);

        setmealDto.setSetmealDishes(setmealDishList);
        setmealDto.setCategoryName(categoryServiceOne.getName());
        return setmealDto;
    }

    /**
     * 更新套餐，同时更新套餐与菜品之间的关联
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
       //更新Setmeal表
        this.updateById(setmealDto);

        //需要将以前表内setDish相关删除
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(lqw);

        //更新SetmealDish表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(setmealDishes);
    }
}
