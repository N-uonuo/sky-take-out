package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;


    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        //设置一个集合，用来存储从begin到end的天数
        List<LocalDate> dates = new ArrayList<>();

        dates.add(begin);

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dates.add(begin);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dates) {
            //查询date日期对应的营业额数据
            /*LocalDateTime.of(date, LocalTime.MIN) 创建了一个 LocalDateTime 对象，
            使用了 date 参数（即循环中的每个日期）作为日期部分，而 LocalTime.MIN 则表示当天的最小时间，即午夜零点。
            所以，beginTime 变量就代表了给定日期的开始时间，即该日期的午夜零点。
           */
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            if (turnover == null) {
                turnover = 0.0;
            }
            turnoverList.add(turnover);

        }


        TurnoverReportVO build = TurnoverReportVO.builder()
                .dateList(StringUtil.join(",", dates))
                .turnoverList(StringUtil.join(",", turnoverList))
                .build();


        return build;
    }

    /**
     * 指定区间内用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        //设置一个集合，用来存储从begin到end的天数
        List<LocalDate> dates = new ArrayList<>();

        dates.add(begin);

        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dates.add(begin);
        }

        //每天新增用户集合
        List<Integer> newUserList = new ArrayList<>();
        //每天的总用户集合
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dates) {
            //查询date日期对应的新增用户数据
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();

            map.put("end", endTime);
            //总用户数
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            //新增用户数
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);

        }

        UserReportVO build = UserReportVO.builder()
                .dateList(StringUtil.join(",", dates))
                .newUserList(StringUtil.join(",", newUserList))
                .totalUserList(StringUtil.join(",", totalUserList))
                .build();


        return build;
    }
}