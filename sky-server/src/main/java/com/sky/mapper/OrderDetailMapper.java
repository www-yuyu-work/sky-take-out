package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 根据订单id查询订单详情
     * @param orderId
     * @return
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> getByOrderId(Long orderId);

    /**
     * 插入订单详情
     * @param orderDetail
     */
    @Insert("insert into order_detail (name, order_id, dish_id, setmeal_id, dish_flavor, number, amount, image) " +
            "values (#{name}, #{orderId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image})")
    void insert(OrderDetail orderDetail);

}
