package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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


    //根据id更新订单
    void update(Orders orders);


    /**
     * 查询各个状态的订单数量
     * @param deliveryInProgress
     * @return
     */
    @Select("select count(*) from sky_take_out.orders where status=#{deliveryInProgress}")
    Integer countStatus(Integer deliveryInProgress);




    //根据订单状态和下单时间查询订单
    @Select("select * from sky_take_out.orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);


    /**
     * 根据订单号查询订单
     * @param orderNumber
     * @return
     */
    @Select("select * from sky_take_out.orders where number=#{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 根据订单状态和下单时间查询订单
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据订单状态和下单时间查询订单
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 查询销售额top10
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> SalesTop10(LocalDateTime begin, LocalDateTime end);


}
