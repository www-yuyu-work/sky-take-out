package com.sky.mapper;

import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 新增菜品
     * @param dish
     */
    void insert(Dish dish);

    /**
     * 菜品分页查询（含分类名称）
     * @param dishPageQueryDTO
     * @return
     */
    List<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据id查询菜品（含分类名称）
     * @param id
     * @return
     */
    DishVO getByIdWithCategory(Long id);

    /**
     * 更新菜品（动态SQL）
     * @param dish
     */
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    /**
     * 根据分类id查询启售菜品
     * @param categoryId
     * @return
     */
    @Select("select * from dish where category_id = #{categoryId} and status = 1")
    List<Dish> getEnabledByCategoryId(Long categoryId);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 查询被套餐关联的菜品数量
     * @param ids
     * @return
     */
    Integer countBySetmealDishIds(List<Long> ids);

    /**
     * 根据状态统计菜品数量
     * @param status
     * @return
     */
    @Select("select count(*) from dish where status = #{status}")
    Integer countByStatus(Integer status);

}
