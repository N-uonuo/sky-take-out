package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void batchInsert(List<DishFlavor> flavors);

   /* @Delete("delete from sky_take_out.dish_flavor where dish_id=#{dish_id}")
    void deleteByDishId(Long dishId);*/

    //根据菜品id集合批量删除菜品口味
    void deleteByDishIds(List<Long> dishIds);
}
