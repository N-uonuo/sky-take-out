package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.pagehelper.PageHelper;


import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    //新增套餐，并且新增套餐和菜品的关联关系
    @Override
    @Transactional//开启事务    保证数据的一致性
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        //将dto转换为entity
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //向套餐表插入一条数据
        setmealMapper.insert(setmeal);
        //获取生成的套餐id
        Long setmealId = setmeal.getId();
        //获取套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //遍历关联关系，设置套餐id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }

        //批量保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        Integer pageNum = setmealPageQueryDTO.getPage();
        Integer pageSize = setmealPageQueryDTO.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());


    }

    /**
     * 批量删除套餐
     *
     * @param ids
     */
    //常用于标识一个方法或类需要被包裹在一个事务中执行。
    // 事务是一组数据库操作，要么全部成功提交，要么全部失败回滚，以确保数据库的一致性和完整性。
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断是否为空
        if (ids == null || ids.size() == 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_SELECTION_IS_EMPTY);
        }

        //判断该套餐是否在起售中
        ids.stream().forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal == null) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_IS_NULL);
            }
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            } else {
                //删除套餐表中的数据
                setmealMapper.deleteById(id);
                //删除套餐菜品关系表中的数据
                setmealDishMapper.deleteBySetmealId(id);
            }
        });
    }

    /**
     * 根据id查询套餐和套餐菜品关系
     *
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        //根据id查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        //根据套餐id查询套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        //将查询到的数据封装到vo中
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);

        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        //将dto转换为entity
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //更新套餐表
        setmealMapper.update(setmeal);
        //获取套餐id
        Long setmealId = setmeal.getId();
        //删除套餐和菜品的关联关系
        setmealDishMapper.deleteBySetmealId(setmealId);
        //获取套餐和菜品的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //遍历关联关系，设置套餐id
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        //批量保存套餐和菜品的关联关系
        setmealDishMapper.insertBatch(setmealDishes);

    }

    @Override
    public void updateStatus(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if (status == StatusConstant.ENABLE) {
            //根据套餐id查询套餐和菜品的关联关系
            List<Dish> dishList = dishMapper.getBySetmealId(id);            //遍历关联关系，判断菜品状态
            if (dishList != null && dishList.size() > 0) {
                dishList.stream().forEach(dish -> {
                    if(StatusConstant.DISABLE == dish.getStatus()){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    //根据分类id查询套餐
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据套餐id查询包含的菜品列表
     *
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}

