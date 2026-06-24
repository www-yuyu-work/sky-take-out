package com.sky.controller.user;

import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端菜品浏览接口
 */
@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 根据分类id查询菜品（含口味）
     */
    @ApiOperation("根据分类id查询菜品")
    @GetMapping("/list")
    public Result<List<DishVO>> getByCategoryId(@RequestParam Long categoryId) {
        log.info("根据分类id查询菜品：{}", categoryId);
        List<DishVO> list = dishService.listWithFlavors(categoryId);
        return Result.success(list);
    }
}
