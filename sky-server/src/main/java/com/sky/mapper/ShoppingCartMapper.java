package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 新增购物车
     * @param shoppingCart
     */
    @Insert("insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param shoppingCart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 根据用户id查询购物车（按创建时间倒序）
     * @param userId
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} order by create_time desc")
    List<ShoppingCart> getByUserId(Long userId);

    /**
     * 根据用户id、菜品id、口味查询购物车（菜品）
     * @param userId
     * @param dishId
     * @param dishFlavor
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and dish_id = #{dishId} and dish_flavor = #{dishFlavor}")
    ShoppingCart getByUserIdAndDishId(Long userId, Long dishId, String dishFlavor);

    /**
     * 根据用户id、套餐id、口味查询购物车（套餐）
     * @param userId
     * @param setmealId
     * @param dishFlavor
     * @return
     */
    @Select("select * from shopping_cart where user_id = #{userId} and setmeal_id = #{setmealId} and dish_flavor = #{dishFlavor}")
    ShoppingCart getByUserIdAndSetmealId(Long userId, Long setmealId, String dishFlavor);

    /**
     * 根据id删除购物车
     * @param id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据用户id清空购物车
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);

}
