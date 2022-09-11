package com.hanhan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hanhan.common.R;
import com.hanhan.dto.DishDto;
import com.hanhan.entity.Category;
import com.hanhan.entity.Dish;
import com.hanhan.entity.FoodFlavor;
import com.hanhan.service.CategoryService;
import com.hanhan.service.FoodFlavorService;
import com.hanhan.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
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
     * @param Dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish Dish){
        log.info("Dish:{}",Dish);

        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(Dish.getName()), com.hanhan.entity.Dish::getName, Dish.getName());
        lqw.eq(null != Dish.getCategoryId(), com.hanhan.entity.Dish::getCategoryId, Dish.getCategoryId());

        //起售状态
        lqw.eq(com.hanhan.entity.Dish::getStatus,1);
        lqw.orderByAsc(com.hanhan.entity.Dish::getSort).orderByDesc(com.hanhan.entity.Dish::getUpdateTime);
        List<Dish> DishList = dishService.list(lqw);

        List<DishDto> list = DishList.stream().map((item) -> {
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

        return R.success(list);
    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,Long ids){
        log.info("status:{},ids:{}",status,ids);
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Dish::getId,ids);

        Dish dishServiceOne = dishService.getOne(lqw);
        dishServiceOne.setStatus(status);

        dishService.updateById(dishServiceOne);
        return R.success("修改售卖状态成功");
    }
}
