package com.hanhan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hanhan.entity.Category;


public interface CategoryService extends IService<Category> {

    /**
     * 删除分类
     * @param ids
     */
    public  void remove(Long ids);
}
