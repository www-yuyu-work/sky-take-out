package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void addDish(DishDTO dishDTO) {
        //1、DTO属性拷贝到实体
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //2、设置默认值
        dish.setStatus(StatusConstant.ENABLE);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());

        //3、插入菜品
        dishMapper.insert(dish);

        //4、获取insert生成的主键，设置到每个口味
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishId);
            }
            //5、批量插入口味
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        //1、开始分页
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        //2、执行查询
        List<DishVO> list = dishMapper.pageQuery(dishPageQueryDTO);

        //3、封装分页结果
        Page<DishVO> page = (Page<DishVO>) list;
        return new PageResult(page.getTotal(), list);
    }

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        //1、查询菜品基本信息（含分类名称）
        DishVO dishVO = dishMapper.getByIdWithCategory(id);
        //2、查询菜品关联的口味
        List<DishFlavor> flavors = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void updateDish(DishDTO dishDTO) {
        //1、DTO属性拷贝到实体
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //2、设置更新时间、修改人
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());

        //3、更新菜品表
        dishMapper.update(dish);

        //4、删除旧口味
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //5、插入新口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            for (DishFlavor flavor : flavors) {
                flavor.setDishId(dishDTO.getId());
            }
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDish(List<Long> ids) {
        //1、判断是否有起售中的菜品
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //2、判断是否被套餐关联
        Integer count = dishMapper.countBySetmealDishIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //3、删除菜品
        dishMapper.deleteByIds(ids);

        //4、删除关联的口味
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 菜品起售、停售
     *
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> getByCategoryId(Long categoryId) {
        return dishMapper.getByCategoryId(categoryId);
    }

    /**
     * 根据分类id查询启售菜品（含口味）
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> listWithFlavors(Long categoryId) {
        //1、查询启售菜品
        List<Dish> dishList = dishMapper.getEnabledByCategoryId(categoryId);

        //2、构建DishVO列表，填充口味
        List<DishVO> dishVOList = new ArrayList<>();
        for (Dish dish : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(dish, dishVO);
            //查询口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(dish.getId());
            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
