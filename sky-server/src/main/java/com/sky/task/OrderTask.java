package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    //处理超时订单
    //@Scheduled(cron = "0/5 * * * * ?")
    @Scheduled(cron = "0 * * * * ?")
    //每分钟执行一次
    public void processTimeOutOrder() {
        log.info("处理超时订单:{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if (orderList != null && orderList.size() > 0) {
            for (Orders order : orderList) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }
    }

    //处理处于派送的订单
    @Scheduled(cron = "0 0 1 * * ?")//每天凌晨1点执行一次
    //@Scheduled(cron = "0/5 * * * * ?")//每分钟执行一次
    public void processDeliveryOrder() {
        log.info("处理超时的订单:{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> orderList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (orderList != null && orderList.size() > 0) {
            orderList.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }

    }


}
