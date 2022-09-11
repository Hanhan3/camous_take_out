package com.hanhan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hanhan.common.R;
import com.hanhan.dto.DishDto;
import com.hanhan.entity.Category;
import com.hanhan.entity.Dish;
import com.hanhan.entity.FoodFlavor;
import com.hanhan.entity.Setmeal;
import com.hanhan.service.CategoryService;
import com.hanhan.service.FoodFlavorService;
import com.hanhan.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private FoodFlavorService foodFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        //清除所有菜品的Redis缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清除新增菜品分类的Redis缓存数据
        String key = "dish_" + dishDto.getCategoryId() +"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        log.info("page = {},pagesize={},name={}",page,pageSize,name);
        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //添加过滤条件
//        lqw.eq(name!=null, Dish::getName,name);
//        此处应使用模糊查询更好
        lqw.like(name != null,Dish::getName,name);
        //排序条件
        lqw.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo, lqw);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //对新对象添加categoryName属性==》将Mercahndise类转为DishDto类
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询商品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto =new DishDto();
        dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改商品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);

        //清除所有菜品的Redis缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清除新增菜品分类的Redis缓存数据
        String key = "dish_" + dishDto.getCategoryId() +"_1";
        redisTemplate.delete(key);

        return R.success("修改成功");
    }

//    /**
//     * 根据条件查询对应的菜品数据
//     * @param categoryId
//     * @return
//     */
//    @GetMapping("/list")
//    public R<List<Dish>> list(Long categoryId){
//        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
//        lqw.eq(Dish::getCategoryId,categoryId);
//
//        //起售状态
//        lqw.eq(Dish::getStatus,1);
//
//        lqw.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> DishList = DishService.list(lqw);
//        return R.success(DishList);
//    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        log.info("Dish:{}",dish);

        //先从Redis中获取菜品数据，如果有则直接返回
        String keys = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        List<DishDto> list = (List<DishDto>) redisTemplate.opsForValue().get(keys);
        if(list != null){
            return R.success(list);
        }


        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(dish.getName()), Dish::getName, dish.getName());
        lqw.eq(null != dish.getCategoryId(), Dish::getCategoryId, dish.getCategoryId());

        //起售状态
        lqw.eq(com.hanhan.entity.Dish::getStatus,1);
        lqw.orderByAsc(com.hanhan.entity.Dish::getSort).orderByDesc(com.hanhan.entity.Dish::getUpdateTime);
        List<Dish> DishList = dishService.list(lqw);

         list = DishList.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);

            //根据id查询分类对象
            Category category = categoryService.getById(item.getCategoryId());
            if(category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //根据id查询口味数据
            LambdaQueryWrapper<FoodFlavor> flavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
            flavorLambdaQueryWrapper.eq(FoodFlavor::getDishId,item.getId());

            dishDto.setFlavors(foodFlavorService.list(flavorLambdaQueryWrapper));
            return dishDto;
        }).collect(Collectors.toList());

        //如果Redis中没有数据，查询数据库，并将查询到的菜品数据写到Redis中
        redisTemplate.opsForValue().set(keys,list,60, TimeUnit.MINUTES);
        return R.success(list);
    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status:{},ids:{}",status,ids);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.in(Dish::getId,ids);

        List<Dish> dishList = dishService.list(lqw);
        dishList.stream().map((item)->{
            item.setStatus(status);
           dishService.updateById(item);
            //清除所有菜品的Redis缓存数据
            //Set keys = redisTemplate.keys("dish_*");
            //redisTemplate.delete(keys);

            //清除新增菜品分类的Redis缓存数据
            String key = "dish_" + item.getCategoryId() +"_1";
            redisTemplate.delete(key);
            return item;
        }).collect(Collectors.toList());

        return R.success("修改售卖状态成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功");
    }
}
