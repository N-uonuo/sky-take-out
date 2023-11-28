package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;


    @Transactional
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常(地址不存在,购物车商品不存在)
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //地址不存在
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //购物车商品不存在
        //查询当前用户的购物车
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        //查询当前用户的购物车
        List<ShoppingCart> shoppingCarts = shoppingCartMapper.list(shoppingCart);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            //购物车商品不存在
            throw new AddressBookBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }


        //向订单表插入一调数据
        Orders orders = new Orders();//new Orders的原因是因为Orders的字段和数据库是映射的
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());

        orderMapper.insert(orders);

        ArrayList<Object> orderDetailList = new ArrayList<>();
        //向订单明细表插入n条数据
        shoppingCarts.forEach(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//设置当前订单明细的订单id
            orderDetailList.add(orderDetail);
        });

        orderDetailMapper.batchInsert(orderDetailList);


        //清空当前用户的购物车数据
        //TODO 应该判断支付后再清空购物车
        shoppingCartMapper.deleteAllByUserId(userId);


        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();


        return orderSubmitVO;
    }

    @Override
    public PageResult pageQuery4User(Integer pageNum, int pageSize, Integer status) {

        if (pageNum == null) {
            pageNum = 1; // 设置默认值为第一页
        }


        //设置分页
        PageHelper.startPage(pageNum, pageSize);

        //查询当前用户的订单,并设置查询条件
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 查询出订单明细，并封装入OrderVO进行响应
        List<OrderVO> orderVOList = new ArrayList();

        if (page != null && page.getTotal() > 0) {
            page.forEach(orders -> {
                Long orderId = orders.getId();//订单id

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                //查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
                orderVO.setOrderDetailList(orderDetails);

                orderVOList.add(orderVO);
            });
        }
        //TODO 看一下PageResult类
        return new PageResult(page.getTotal(), orderVOList);
    }


    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {

        // 根据id查询订单
        Orders orders = orderMapper.getById(id);

        // 根据订单id查询订单明细
        List<OrderDetail> orderDetail = orderDetailMapper.getByOrderId(id);

        // 封装OrderVO
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetail);

        return orderVO;
    }

    /**
     * 用户取消订单
     *
     * @param id
     */
    @Override
    public void userCancelById(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        //判断订单是否存在
        if (ordersDB == null) {
            throw new AddressBookBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //判断订单状态是否为待付款
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus() > 2){
            throw new AddressBookBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //new一个Orders对象，用于更新订单状态,因为orderDB是从数据库中查询出来的，所以不能直接修改
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        // 订单处于待接单状态下取消，需要进行退款
        if (ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用微信支付退款接口
            //TODO 调用微信支付退款接口

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 订单处于待付款状态下取消，不需要进行退款
        if (ordersDB.getStatus().equals(Orders.PENDING_PAYMENT)) {
            //支付状态修改为 未支付
            orders.setPayStatus(Orders.UN_PAID);
        }


        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);



    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        // 获取当前订单
        Orders ordersDB = orderMapper.getById(id);

        /*// 判断订单是否存在
        if (ordersDB == null) {
            throw new AddressBookBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }*/

        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        // 将订单详情中的商品信息添加到购物车
        // TODO 反复调用insert方法，性能不好，可以考虑批量插入
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(orderDetail.getNumber());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

}
