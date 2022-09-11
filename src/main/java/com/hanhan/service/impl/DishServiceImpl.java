package com.hanhan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanhan.common.CustomException;
import com.hanhan.dto.DishDto;
import com.hanhan.entity.Dish;
import com.hanhan.entity.FoodFlavor;
import com.hanhan.entity.Setmeal;
import com.hanhan.entity.SetmealDish;
import com.hanhan.mapper.DishMapper;
import com.hanhan.service.FoodFlavorService;
import com.hanhan.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private FoodFlavorService foodFlavorService;

    /**
     * 添加菜品口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long merchandiseId = dishDto.getId();
        //菜品口味
        List<FoodFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(merchandiseId);
            return item;
        }).collect(Collectors.toList());
        //保存菜品口味数据到菜品口味表dish_flavor
        foodFlavorService.saveBatch(flavors);
    }

    /**
     * 查询商品及其对应口味
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
       Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前商品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<FoodFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FoodFlavor::getDishId,dish.getId());
        List<FoodFlavor> flavors = foodFlavorService.list(lqw);

        dishDto.setFlavors(flavors);
        return dishDto;
    }

    @Override
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应口味数据---dish_flavor表的delete操作
        LambdaQueryWrapper<FoodFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FoodFlavor::getDishId,dishDto.getId());
        foodFlavorService.remove(lqw);

        //添加当前提交过来的口味数据---dish_flavor表的insert操作
        List<FoodFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        foodFlavorService.saveBatch(flavors);
    }

    /**
     * 删除菜品及其口味
     * @param ids
     */
    @Override
    public void deleteWithFlavor(List<Long> ids) {
        //判断是否为启售状态，若是，则不能删除
        LambdaQueryWrapper<Dish> dishlqw = new LambdaQueryWrapper<>();
        dishlqw.in(Dish::getId,ids);
        dishlqw.eq(Dish::getStatus,1);
        int count = this.count(dishlqw);
        if(count >0){
            throw new CustomException("商品正在售卖中，不能删除");
        }
        //如可以删除，从dish表中删除
        this.removeByIds(ids);
        //从setmealDIsh表中删除
        LambdaQueryWrapper<FoodFlavor> lqw = new LambdaQueryWrapper<>();
        lqw.in(FoodFlavor::getDishId,ids);
        foodFlavorService.remove(lqw);
    }
}
