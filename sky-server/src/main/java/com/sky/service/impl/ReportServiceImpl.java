package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 营业额统计
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询每日营业额
        List<Map<String, Object>> dailyData = orderMapper.getDailyTurnover(beginTime, endTime);
        Map<String, Double> dateMap = dailyData.stream().collect(
                Collectors.toMap(m -> (String) m.get("orderDate"), m -> ((Number) m.get("turnover")).doubleValue()));

        // 生成日期范围内每天的列表
        List<String> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            turnoverList.add(dateMap.getOrDefault(dateStr, 0.0));
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(turnoverList.stream().map(Object::toString).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 用户统计
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询每日新增用户
        List<Map<String, Object>> dailyData = userMapper.getDailyNewUsers(beginTime, endTime);
        Map<String, Integer> newUserMap = dailyData.stream().collect(
                Collectors.toMap(m -> (String) m.get("userDate"), m -> ((Number) m.get("newUsers")).intValue()));

        // 生成每日数据，计算累计总用户
        List<String> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        int cumulativeTotal = userMapper.countTotalUsersByDay(beginTime.minusDays(1));

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            int newUsers = newUserMap.getOrDefault(dateStr, 0);
            newUserList.add(newUsers);
            cumulativeTotal += newUsers;
            totalUserList.add(cumulativeTotal);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .newUserList(newUserList.stream().map(Object::toString).collect(Collectors.joining(",")))
                .totalUserList(totalUserList.stream().map(Object::toString).collect(Collectors.joining(",")))
                .build();
    }

    /**
     * 订单统计
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 每日全部订单
        List<Map<String, Object>> dailyOrders = orderMapper.getDailyOrderCount(beginTime, endTime);
        Map<String, Integer> orderCountMap = dailyOrders.stream().collect(
                Collectors.toMap(m -> (String) m.get("orderDate"), m -> ((Number) m.get("orderCount")).intValue()));

        // 每日有效订单（已完成）
        List<Map<String, Object>> dailyValid = orderMapper.getDailyValidOrderCount(beginTime, endTime);
        Map<String, Integer> validCountMap = dailyValid.stream().collect(
                Collectors.toMap(m -> (String) m.get("orderDate"), m -> ((Number) m.get("validOrderCount")).intValue()));

        List<String> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        int totalOrders = 0;
        int totalValid = 0;

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String dateStr = d.toString();
            dateList.add(dateStr);
            int count = orderCountMap.getOrDefault(dateStr, 0);
            int valid = validCountMap.getOrDefault(dateStr, 0);
            orderCountList.add(count);
            validOrderCountList.add(valid);
            totalOrders += count;
            totalValid += valid;
        }

        double completionRate = totalOrders > 0 ? (double) totalValid / totalOrders : 0.0;

        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(orderCountList.stream().map(Object::toString).collect(Collectors.joining(",")))
                .validOrderCountList(validOrderCountList.stream().map(Object::toString).collect(Collectors.joining(",")))
                .totalOrderCount(totalOrders)
                .validOrderCount(totalValid)
                .orderCompletionRate(completionRate)
                .build();
    }

    /**
     * 销量排名Top10
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Map<String, Object>> top10 = orderMapper.getTop10(beginTime, endTime);
        List<String> nameList = top10.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
        List<String> numberList = top10.stream().map(m -> m.get("totalNumber").toString()).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    /**
     * 导出Excel报表
     */
    @Override
    public void exportBusinessData(HttpServletResponse response) {
        // 查询近30天数据
        LocalDate end = LocalDate.now();
        LocalDate begin = end.minusDays(30);
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 查询营业额和订单数据
        List<Map<String, Object>> dailyTurnover = orderMapper.getDailyTurnover(beginTime, endTime);
        List<Map<String, Object>> dailyOrders = orderMapper.getDailyOrderCount(beginTime, endTime);
        List<Map<String, Object>> dailyValid = orderMapper.getDailyValidOrderCount(beginTime, endTime);

        Map<String, Double> turnoverMap = new LinkedHashMap<>();
        Map<String, Integer> orderMap = new LinkedHashMap<>();
        Map<String, Integer> validMap = new LinkedHashMap<>();

        for (LocalDate d = begin; !d.isAfter(end); d = d.plusDays(1)) {
            String ds = d.toString();
            turnoverMap.put(ds, 0.0);
            orderMap.put(ds, 0);
            validMap.put(ds, 0);
        }
        dailyTurnover.forEach(m -> turnoverMap.put((String) m.get("orderDate"), ((Number) m.get("turnover")).doubleValue()));
        dailyOrders.forEach(m -> orderMap.put((String) m.get("orderDate"), ((Number) m.get("orderCount")).intValue()));
        dailyValid.forEach(m -> validMap.put((String) m.get("orderDate"), ((Number) m.get("validOrderCount")).intValue()));

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx")) {
            XSSFWorkbook workbook = in != null ? new XSSFWorkbook(in) : new XSSFWorkbook();
            XSSFSheet sheet = workbook.getSheetAt(0);

            int rowIdx = 2;
            for (String date : turnoverMap.keySet()) {
                XSSFRow row = sheet.getRow(rowIdx);
                if (row == null) row = sheet.createRow(rowIdx);
                row.getCell(0).setCellValue(date);
                row.getCell(1).setCellValue(turnoverMap.get(date));
                row.getCell(2).setCellValue(orderMap.get(date));
                row.getCell(3).setCellValue(validMap.get(date));
                rowIdx++;
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=report.xlsx");
            ServletOutputStream out = response.getOutputStream();
            workbook.write(out);
            out.flush();
            workbook.close();
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }

}
