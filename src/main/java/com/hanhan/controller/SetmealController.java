package com.hanhan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hanhan.common.R;
import com.hanhan.dto.DishDto;
import com.hanhan.dto.SetmealDto;
import com.hanhan.entity.Dish;
import com.hanhan.entity.Setmeal;
import com.hanhan.entity.SetmealDish;
import com.hanhan.service.SetmealDishService;
import com.hanhan.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;


    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("保存成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        //查询构造器
        LambdaQueryWrapper<Setmeal> lqw =new LambdaQueryWrapper<>();
        lqw.like(name != null, Setmeal::getName,name);

        lqw.orderByDesc(Setmeal::getUpdateTime);

        setmealService.page(pageInfo,lqw);
        return R.success(pageInfo);
    }

    /**
     * 查询套餐详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        log.info("id:{}",id);
        return R.success(setmealService.getByIdWithDish(id));
    }


    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    /**
     * 获取全部套餐信息
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(null !=setmeal.getCategoryId(), Setmeal::getCategoryId,setmeal.getCategoryId());
        lqw.eq(Setmeal::getStatus,setmeal.getStatus());
        lqw.orderByDesc(Setmeal::getUpdateTime);

        return R.success(setmealService.list(lqw));
    }

    /**
     * 修改套餐信息
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info("SetDto:{}",setmealDto);
        setmealService.updateWithDish(setmealDto);

        return R.success("修改成功");
    }

    /**
     * 修改套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status:{},ids:{}",status,ids);
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.in(Setmeal::getId,ids);

        List<Setmeal> setmealList = setmealService.list(lqw);
        setmealList.stream().map((item)->{
            item.setStatus(status);
            setmealService.updateById(item);
            return item;
        }).collect(Collectors.toList());

        return R.success("修改售卖状态成功");
    }
}
