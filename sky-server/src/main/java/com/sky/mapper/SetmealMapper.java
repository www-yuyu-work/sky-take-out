package com.sky.mapper;

import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 新增套餐
     * @param setmeal
     */
    void insert(Setmeal setmeal);

    /**
     * 套餐分页查询（含分类名称）
     * @param setmealPageQueryDTO
     * @return
     */
    List<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 根据id查询套餐（含分类名称）
     * @param id
     * @return
     */
    SetmealVO getByIdWithCategory(Long id);

    /**
     * 更新套餐
     * @param setmeal
     */
    void update(Setmeal setmeal);

    /**
     * 批量删除套餐
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据状态统计套餐数量
     * @param status
     * @return
     */
    @Select("select count(*) from setmeal where status = #{status}")
    Integer countByStatus(Integer status);

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @Select("select * from setmeal where category_id = #{categoryId} and status = 1")
    List<Setmeal> getByCategoryId(Long categoryId);

}
