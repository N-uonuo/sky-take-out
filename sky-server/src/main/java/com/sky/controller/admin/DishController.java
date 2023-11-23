package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;//这个对象是用来操作redis的，比如存取数据，删除数据等

    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品", dishDTO);
        dishService.saveWithFlavor(dishDTO);

        //清除缓存数据
        String key = "dish_" + dishDTO.getCategoryId();
        clearCache(key);
        return Result.success(dishDTO);
    }

    @ApiOperation("分页查询菜品")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询菜品", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        log.info("删除菜品", ids);
        dishService.deleteBatch(ids);

        clearCache("dish_*");//清除所有菜品缓存数据

       /* redisTemplate: 这是 Spring Data Redis 框架提供的一个模板类，用于简化 Redis 操作。
        keys("dish_*"): 这是 RedisTemplate 提供的方法之一，用于匹配符合指定模式的键。在这里，模式是 "dish_"，它使用通配符 "" 表示匹配任意字符序列。这意味着它会匹配所有以 "dish_" 开头的键。
        Set keys = ...: 获取匹配的键集合，并将其存储在一个 Set 对象中。Set 是 Java 中表示不重复元素集合的数据结构，这里用于存储匹配到的 Redis 键。
        */
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {//别忘了加路径参数注解
        log.info("根据id查询菜品:{}", id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("更新菜品:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        clearCache("dish_*");//清除所有菜品缓存数据


        return Result.success();
    }

    //菜品的起售和停售
    @PostMapping("/status/{status}")
    @ApiOperation("菜品的起售和停售")
    public Result<String> updateStatus(@PathVariable Integer status,Long id ){
        log.info("菜品的起售和停售:{}",id,status);

        dishService.updateStatus(id,status);

        clearCache("dish_*");//清除所有菜品缓存数据


        return Result.success();
    }




    //根据分类id查询菜品。注意是  分类id  不是菜品id
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("根据分类id查询菜品:{}",categoryId);
        List<Dish> dishList=dishService.list(categoryId);
        return Result.success(dishList);
    }

    //清理缓存数据
    public void clearCache(String pattern){
        //将所有菜品缓存数据全部清除，因为不知道哪些菜品属于哪些分类
        Set keys = redisTemplate.keys(pattern);//.forEach(key -> redisTemplate.delete(key) 无需遍历，直接删除
        //delete方法可以删除多个key,支持Collection<K>
        redisTemplate.delete(keys);
    }
}
