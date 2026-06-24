package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车：{}", shoppingCartDTO);

        //1、获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //2、判断是菜品还是套餐
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());

        if (shoppingCartDTO.getDishId() != null) {
            //添加的是菜品
            Dish dish = dishMapper.getById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());

            //查询购物车中是否已有同一菜品同一口味
            ShoppingCart existing = shoppingCartMapper.getByUserIdAndDishId(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor());
            if (existing != null) {
                //已存在，数量加1
                existing.setNumber(existing.getNumber() + 1);
                shoppingCartMapper.updateNumberById(existing);
            } else {
                //不存在，新增
                shoppingCartMapper.insert(shoppingCart);
            }
        } else if (shoppingCartDTO.getSetmealId() != null) {
            //添加的是套餐
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());

            //查询购物车中是否已有同一套餐同一口味
            ShoppingCart existing = shoppingCartMapper.getByUserIdAndSetmealId(userId, shoppingCartDTO.getSetmealId(), shoppingCartDTO.getDishFlavor());
            if (existing != null) {
                //已存在，数量加1
                existing.setNumber(existing.getNumber() + 1);
                shoppingCartMapper.updateNumberById(existing);
            } else {
                //不存在，新增
                shoppingCartMapper.insert(shoppingCart);
            }
        }
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        log.info("删除购物车中一个商品：{}", shoppingCartDTO);

        //1、获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //2、判断是菜品还是套餐，查询购物车中已有商品
        ShoppingCart existing;
        if (shoppingCartDTO.getDishId() != null) {
            existing = shoppingCartMapper.getByUserIdAndDishId(userId, shoppingCartDTO.getDishId(), shoppingCartDTO.getDishFlavor());
        } else {
            existing = shoppingCartMapper.getByUserIdAndSetmealId(userId, shoppingCartDTO.getSetmealId(), shoppingCartDTO.getDishFlavor());
        }

        if (existing == null) {
            return;
        }

        //3、判断数量：等于1则删除，大于1则减1
        if (existing.getNumber() == 1) {
            shoppingCartMapper.deleteById(existing.getId());
        } else {
            existing.setNumber(existing.getNumber() - 1);
            shoppingCartMapper.updateNumberById(existing);
        }
    }

    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> getShoppingCartList() {
        log.info("查看购物车");

        Long userId = BaseContext.getCurrentId();
        return shoppingCartMapper.getByUserId(userId);
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        log.info("清空购物车");

        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

}
