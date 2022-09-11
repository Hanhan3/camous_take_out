package com.hanhan.dto;

import com.hanhan.entity.Dish;
import com.hanhan.entity.FoodFlavor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<FoodFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
