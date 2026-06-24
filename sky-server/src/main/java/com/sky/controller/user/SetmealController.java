package com.sky.controller.user;

import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.mapper.SetmealDishMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端套餐浏览接口
 */
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Slf4j
@Api(tags = "C端套餐浏览接口")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 根据分类id查询套餐
     */
    @ApiOperation("根据分类id查询套餐")
    @GetMapping("/list")
    public Result<List<Setmeal>> getByCategoryId(@RequestParam Long categoryId) {
        log.info("根据分类id查询套餐：{}", categoryId);
        List<Setmeal> list = setmealService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 根据套餐id查询包含的菜品
     */
    @ApiOperation("根据套餐id查询包含的菜品")
    @GetMapping("/dish/{id}")
    public Result<List<DishItemVO>> getDishBySetmealId(@PathVariable Long id) {
        log.info("根据套餐id查询包含的菜品：{}", id);
        List<DishItemVO> list = setmealDishMapper.getDishItemBySetmealId(id);
        return Result.success(list);
    }
}
