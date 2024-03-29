package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    //新增菜品
     void saveWithFlavor(DishDTO dishDTO);

    //分页查询菜品
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    //根据菜品查询对应的菜品和口味数据
    DishVO getByIdWithFlavor(Long id);

    //修改菜品
    void updateWithFlavor(DishDTO dishDTO);
}
