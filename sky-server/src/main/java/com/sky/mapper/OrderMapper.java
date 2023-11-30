package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {


    /**
     * 插入订单
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 分页条件查询并按下单时间排序
     * @param ordersPageQueryDTO
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     * @param id
     */
    @Select("select * from sky_take_out.orders where id=#{id}")
    Orders getById(Long id);


    void update(Orders orders);


    /**
     * 查询各个状态的订单数量
     * @param deliveryInProgress
     * @return
     */
    @Select("select count(*) from sky_take_out.orders where status=#{deliveryInProgress}")
    Integer countStatus(Integer deliveryInProgress);
}
