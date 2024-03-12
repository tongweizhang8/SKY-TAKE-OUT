package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
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

    /**
     * 统计指定时间内的营业额数据
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate beginTime, LocalDate endTime) {
        //当前集合用于存放begin到end范围内的每天日期
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(beginTime);
        while (!dataList.equals(endTime)) {
            beginTime = beginTime.plusDays(1);
            dataList.add(beginTime);
        }
        //存放每天营业额
        List<Double> turnoverList = new ArrayList<>();
        for(LocalDate date : dataList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
            map.put("beginTime", begin);
            map.put("endTime", end);
            map.put("status", Orders.COMPLETED);
            Double tunover = orderMapper.sumByMap(map);
            tunover = tunover == null ? 0 : tunover;
            turnoverList.add(tunover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dataList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的用户数据
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate beginTime, LocalDate endTime) {
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(beginTime);
        while (!dataList.equals(endTime)) {
            beginTime = beginTime.plusDays(1);
            dataList.add(beginTime);
        }
        //存放每天新增用户的数量
        List<Integer> newUserList = new ArrayList<>();
        //存放每天总用户的数量
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dataList) {
            LocalDateTime begin = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date,LocalTime.MAX);

            Map map = new HashMap();
            map.put("endTime",end);
            //总用户数量
            Integer totalUser = userMapper.countByMap(map);
            map.put("beginTime",begin);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dataList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 统计指定时间区间内的订单数据
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate beginTime, LocalDate endTime) {
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(beginTime);
        while (!dataList.equals(endTime)) {
            beginTime = beginTime.plusDays(1);
            dataList.add(beginTime);
        }
        //遍历dataList集合查询每天的有效订单数量和订单总数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate date :dataList) {
            //存放每天的订单总数
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);
            Integer orderCount = countByMap(begin, end, null);
            //存放每天有效的订单数，status
            Integer ValidOrderCount = countByMap(begin, end, Orders.COMPLETED);
            orderCountList.add(orderCount);
            validOrderCountList.add(ValidOrderCount);
        }
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if (validOrderCount != null) {
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dataList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .build();
    }

    private Integer countByMap(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);
        return orderMapper.countByMap(map);

    }

    /**
     * 统计指定时间区间内的销售前10的商品数据
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate beginTime, LocalDate endTime) {
        LocalDateTime begin = LocalDateTime.of(beginTime, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(endTime, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop = orderMapper.getSalesTop(begin, end);
        List<String> name = salesTop.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList = StringUtils.join(name, ",");
        List<Integer> number = salesTop.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList = StringUtils.join(number, ",");
        return SalesTop10ReportVO
                .builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    @Override
    public void export(HttpServletResponse response) {
        //1.查询数据库，获取运营数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);//今天减30天
        LocalDate dateEnd = LocalDate.now().minusDays(1);//今天减1天
        LocalDateTime localDateTimeBegin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime localDateTimeEnd = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(localDateTimeBegin, localDateTimeEnd);
        //2.通过poi将数据库写入到excel文件中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(in);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
