package com.hanhan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hanhan.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
