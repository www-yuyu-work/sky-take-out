package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C端分类接口
 */
@RestController("userCategoryController")
@RequestMapping("/user/category")
@Slf4j
@Api(tags = "C端分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 条件查询分类
     */
    @ApiOperation("条件查询分类")
    @GetMapping("/list")
    public Result<List<Category>> getByType(@RequestParam(required = false) Integer type) {
        log.info("查询分类：type={}", type);
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }
}
