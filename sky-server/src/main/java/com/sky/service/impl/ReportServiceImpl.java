package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.Internal;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;


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

    /**
     * 指定区间内订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //设置一个集合，用来存储从begin到end的天数
        List<LocalDate> dates = new ArrayList<>();
        dates.add(begin);
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            dates.add(begin);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        //遍历每天的日期，查询每天的有效订单和总订单
        for (LocalDate date : dates) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //每天的总订单
            Integer totalOrderCount = getCountOrder(beginTime, endTime, null);

            //每天有效的订单
            Integer validOrderCount = getCountOrder(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(totalOrderCount);
            validOrderCountList.add(validOrderCount);

        }

        //总订单数
        int sum = orderCountList.stream().mapToInt(Integer::intValue).sum();
        //有效订单数
        int validSum = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        //订单完成率
        double orderCompletionRate = 0.0;
        if (sum != 0) {
            orderCompletionRate = (double) validSum / sum;
        }

        OrderReportVO build = OrderReportVO.builder()
                .dateList(StringUtil.join(",", dates))
                .orderCountList(StringUtil.join(",", orderCountList))
                .validOrderCountList(StringUtil.join(",", validOrderCountList))
                .totalOrderCount(sum)
                .validOrderCount(validSum)
                .orderCompletionRate(orderCompletionRate)
                .build();


        return build;
    }

    private Integer getCountOrder(LocalDateTime date, LocalDateTime end, Integer status) {
        //ordersStatistics方法使用
        Map map = new HashMap();
        map.put("begin", date);
        map.put("end", end);
        map.put("status", status);
        Integer count = orderMapper.countByMap(map);
        if (count == null) {
            count = 0;
        }
        return count;
    }

    /**
     * 指定区间内销售额top10
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.SalesTop10(beginTime, endTime);

        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtil.join(",", names);

        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtil.join(",", numbers);

        SalesTop10ReportVO build = SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();


        return build;
    }

    /**
     * 导出营业额统计数据
     *
     * @param response
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        //查询数据库，获取营业数据（30天）
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);

        LocalDateTime of = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime of1 = LocalDateTime.of(end, LocalTime.MAX);

        //查询概览数据
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(of, of1);

        //通过POI将数据写入到Excel文件中
        //反射获取模板文件，通过输入流读取模板文件
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/business_data.xlsx");


        try {
            //基于模板创建一个工作簿
            XSSFWorkbook excel = new XSSFWorkbook(resourceAsStream);
            //获取第一个工作表
            XSSFSheet sheet1 = excel.getSheet("sheet1");
            //填充时间
            sheet1.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);

            //获取第4行
            XSSFRow row = sheet1.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获取第5行
            row = sheet1.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for(int i = 0;i < 30; i++){
                LocalDate date = begin.plusDays(i);
                //查询date日期对应的营业额数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获取某一行
                row = sheet1.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //通过输入流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭流
            out.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
