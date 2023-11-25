package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前购物车内菜品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果存在，更新数量
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            //如果不存在，添加购物车
            //判断菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());

            } else {
                //套餐
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //判断当前购物车内菜品是否已经存在
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //如果存在，更新数量
        if (list != null && list.size() > 0) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart);
            if (cart.getNumber() == 0) {
                Long id = cart.getId();
                shoppingCartMapper.deleteById(id);
            }
        }
    }


    /**
     * 查看购物车
     *
     * @param
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        //获取当前微信用户id
        Long userId = BaseContext.getCurrentId();
        ShoppingCart build = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(build);
        return list;
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        //获取当前微信用户id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteAllByUserId(userId);

    }
}
