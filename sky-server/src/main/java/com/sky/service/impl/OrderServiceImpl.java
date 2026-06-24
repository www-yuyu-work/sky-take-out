package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    /**
     * 订单搜索
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> list = orderMapper.conditionSearch(ordersPageQueryDTO);
        Page<Orders> page = (Page<Orders>) list;
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getDetails(Long id) {
        //1、查询订单基本信息
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //2、查询订单菜品详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        //3、拼装订单菜品字符串
        String orderDishes = orderDetailList.stream()
                .map(d -> d.getName() + "*" + d.getNumber() + ";")
                .collect(Collectors.joining());

        //4、组装返回VO
        OrderVO orderVO = new OrderVO();
        org.springframework.beans.BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDishes(orderDishes);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 接单
     *
     * @param ordersConfirmDTO
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     *
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        // 只有待接单(2)的订单可以拒单
        Orders dbOrders = orderMapper.getById(ordersRejectionDTO.getId());
        if (dbOrders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (!dbOrders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     *
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 各状态订单数量统计
     *
     * @return
     */
    @Override
    public Map<String, Integer> statistics() {
        Map<String, Integer> result = new HashMap<>();
        result.put("toBeConfirmed", orderMapper.countByStatus(Orders.TO_BE_CONFIRMED));
        result.put("confirmed", orderMapper.countByStatus(Orders.CONFIRMED));
        result.put("deliveryInProgress", orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS));
        return result;
    }

    /**
     * 用户下单
     *
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单：{}", ordersSubmitDTO);

        //1、获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //2、查询地址信息
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //3、查询购物车数据
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.getByUserId(userId);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //4、构建订单实体
        Orders orders = Orders.builder()
                .number(System.currentTimeMillis() + "")
                .status(Orders.TO_BE_CONFIRMED)
                .userId(userId)
                .addressBookId(ordersSubmitDTO.getAddressBookId())
                .orderTime(LocalDateTime.now())
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .amount(ordersSubmitDTO.getAmount())
                .remark(ordersSubmitDTO.getRemark())
                .userName(addressBook.getConsignee())
                .phone(addressBook.getPhone())
                .address(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail())
                .consignee(addressBook.getConsignee())
                .estimatedDeliveryTime(ordersSubmitDTO.getEstimatedDeliveryTime())
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .packAmount(ordersSubmitDTO.getPackAmount())
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();

        //5、插入订单
        orderMapper.insert(orders);

        //6、构建订单详情列表并插入
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(cart.getName())
                    .orderId(orders.getId())
                    .dishId(cart.getDishId())
                    .setmealId(cart.getSetmealId())
                    .dishFlavor(cart.getDishFlavor())
                    .number(cart.getNumber())
                    .amount(cart.getAmount())
                    .image(cart.getImage())
                    .build();
            orderDetailList.add(orderDetail);
            orderDetailMapper.insert(orderDetail);
        }

        //7、清空购物车
        shoppingCartMapper.deleteByUserId(userId);

        //8、返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
    }

    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, Integer status) {
        log.info("历史订单查询：page={}, pageSize={}, status={}", page, pageSize, status);

        Long userId = BaseContext.getCurrentId();

        PageHelper.startPage(page, pageSize);

        // 如果指定了状态，使用conditionSearch按状态过滤
        if (status != null) {
            OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
            queryDTO.setUserId(userId);
            queryDTO.setStatus(status);
            List<Orders> list = orderMapper.conditionSearch(queryDTO);
            Page<Orders> pg = (Page<Orders>) list;
            return new PageResult(pg.getTotal(), list);
        }

        // 未指定状态，获取全部订单
        List<Orders> list = orderMapper.getByUserId(userId);
        Page<Orders> pg = (Page<Orders>) list;
        return new PageResult(pg.getTotal(), list);
    }

    /**
     * 查询订单详情（用户端）
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO getOrderDetail(Long id) {
        log.info("查询订单详情（用户端）：id={}", id);
        return getDetails(id);
    }

    /**
     * 取消订单（用户端）
     *
     * @param id
     */
    @Override
    public void cancelOrder(Long id) {
        log.info("取消订单（用户端）：id={}", id);

        // 查询订单是否存在
        Orders dbOrders = orderMapper.getById(id);
        if (dbOrders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 催单
     *
     * @param id
     */
    @Override
    public void reminder(Long id) {
        log.info("催单：id={}", id);

        Orders dbOrders = orderMapper.getById(id);
        if (dbOrders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // 催单：记录日志即可，后续可扩展为推送通知
    }

    /**
     * 再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        log.info("再来一单：id={}", id);

        Long userId = BaseContext.getCurrentId();

        // 查询原订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        if (orderDetailList == null || orderDetailList.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 将每个订单详情转为购物车项插入
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = ShoppingCart.builder()
                    .name(orderDetail.getName())
                    .userId(userId)
                    .dishId(orderDetail.getDishId())
                    .setmealId(orderDetail.getSetmealId())
                    .dishFlavor(orderDetail.getDishFlavor())
                    .number(orderDetail.getNumber())
                    .amount(orderDetail.getAmount())
                    .image(orderDetail.getImage())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCartMapper.insert(shoppingCart);
        }
    }

}
