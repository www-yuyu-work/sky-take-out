package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询今日运营数据
     *
     * @return
     */
    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 营业额
        Double turnover = orderMapper.getTurnover(begin, end);

        // 有效订单数
        Integer validOrderCount = orderMapper.countValidOrders(begin, end);

        // 总订单数
        Integer totalOrders = orderMapper.countTotalOrders(begin, end);

        // 订单完成率
        Double orderCompletionRate = totalOrders > 0 ? validOrderCount.doubleValue() / totalOrders : 0.0;

        // 平均客单价
        Double unitPrice = validOrderCount > 0 ? turnover / validOrderCount : 0.0;

        // 新增用户数
        Integer newUsers = userMapper.countNewUsers(begin, end);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    @Override
    public DishOverViewVO getDishOverView() {
        Integer sold = dishMapper.countByStatus(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.countByStatus(StatusConstant.DISABLE);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Integer sold = setmealMapper.countByStatus(StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.countByStatus(StatusConstant.DISABLE);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询订单管理数据
     *
     * @return
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        Integer waitingOrders = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer completedOrders = orderMapper.countByStatus(Orders.COMPLETED);
        Integer cancelledOrders = orderMapper.countByStatus(Orders.CANCELLED);
        Integer allOrders = waitingOrders + deliveredOrders + completedOrders + cancelledOrders
                + orderMapper.countByStatus(Orders.PENDING_PAYMENT)
                + orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

}
