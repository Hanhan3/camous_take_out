package com.hanhan.dto;

import com.hanhan.entity.Setmeal;
import com.hanhan.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
