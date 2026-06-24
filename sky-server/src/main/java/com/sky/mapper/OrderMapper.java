package com.sky.mapper;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper {

    /**
     * 根据id查询订单
     * @param id
     * @return
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 更新订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 订单搜索（分页）
     * @param ordersPageQueryDTO
     * @return
     */
    List<Orders> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 统计某状态的订单数量
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 插入订单
     * @param orders
     */
    @Insert("insert into orders (number, status, user_id, address_book_id, order_time, pay_method, pay_status, amount, remark, " +
            "user_name, phone, address, consignee, estimated_delivery_time, delivery_status, pack_amount, tableware_number, tableware_status) " +
            "values (#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{payMethod}, #{payStatus}, #{amount}, #{remark}, " +
            "#{userName}, #{phone}, #{address}, #{consignee}, #{estimatedDeliveryTime}, #{deliveryStatus}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

    /**
     * 根据用户id查询订单列表
     * @param userId
     * @return
     */
    @Select("select * from orders where user_id = #{userId} order by order_time desc")
    List<Orders> getByUserId(Long userId);

    /**
     * 统计今日某状态的订单数量
     * @param status
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from orders where status = #{status} and order_time between #{begin} and #{end}")
    Integer countByStatusAndDate(Integer status, java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 今日有效订单数（已完成）
     */
    @Select("select count(*) from orders where status = 5 and order_time between #{begin} and #{end}")
    Integer countValidOrders(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 今日营业额（已完成订单金额合计）
     */
    @Select("select ifnull(sum(amount), 0) from orders where status = 5 and order_time between #{begin} and #{end}")
    Double getTurnover(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 今日总订单数
     */
    @Select("select count(*) from orders where order_time between #{begin} and #{end}")
    Integer countTotalOrders(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 每日营业额统计
     */
    java.util.List<java.util.Map<String, Object>> getDailyTurnover(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 每日订单数统计（全部订单）
     */
    java.util.List<java.util.Map<String, Object>> getDailyOrderCount(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 每日有效订单数统计
     */
    java.util.List<java.util.Map<String, Object>> getDailyValidOrderCount(java.time.LocalDateTime begin, java.time.LocalDateTime end);

    /**
     * 销量排名Top10
     */
    java.util.List<java.util.Map<String, Object>> getTop10(java.time.LocalDateTime begin, java.time.LocalDateTime end);

}
