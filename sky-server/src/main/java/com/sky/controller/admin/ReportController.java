package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 营业额统计
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/turnoverStstistics")
    @ApiOperation("营业额统计")
    public Result<TurnoverReportVO> turnoverStstistics(
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
        @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime) {
        log.info("开始统计营业额数据，开始时间：{}，结束时间：{}", beginTime, endTime);
        reportService.getTurnoverStatistics(beginTime, endTime);
            return Result.success(reportService.getTurnoverStatistics(beginTime, endTime));
    }

    /**
     * 用户统计
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/userStstistics")
    @ApiOperation(value = "用户统计")
    public Result<UserReportVO> userStstistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime)  {
        log.info("开始统计用户数据，开始时间：{}，结束时间：{}", beginTime, endTime);
        return Result.success(reportService.getUserStatistics(beginTime, endTime));
    }

    /**
     * 订单统计
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/ordersStstistics")
    @ApiOperation(value = "订单统计")
    public Result<OrderReportVO> ordersStstistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime)  {
        log.info("开始统计用户数据，开始时间：{}，结束时间：{}", beginTime, endTime);
        return Result.success(reportService.getOrderStatistics(beginTime, endTime));
    }

    /**
     * 销量前10统计
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/ordersStstistics")
    @ApiOperation(value = "销量前10统计")
    public Result<SalesTop10ReportVO> top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginTime,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime)  {
        log.info("开始统计用户数据，开始时间：{}，结束时间：{}", beginTime, endTime);
        return Result.success(reportService.getSalesTop10(beginTime, endTime));
    }

    /**
     * 导出运营数据报表
     * @param response
     */
    public void export(HttpServletResponse response) {
        reportService.export(response);
    }
}
