package com.sky.mapper;

import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     * @param category
     */
    void insert(Category category);

    /**
     * 修改分类
     * @param category
     */
    void update(Category category);

    /**
     * 根据id查询分类
     * @param id
     * @return
     */
    @Select("select * from category where id = #{id}")
    Category getById(Long id);

    /**
     * 根据id删除分类
     * @param id
     */
    @org.apache.ibatis.annotations.Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    /**
     * 分类分页查询
     * @param categoryPageQueryDTO
     * @return
     */
    List<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /**
     * 根据类型查询分类
     * @param type
     * @return
     */
    @Select("select * from category where type = #{type} order by sort asc")
    List<Category> getByType(Integer type);

    /**
     * 根据类型和名称统计分类数量（名称唯一性校验）
     * @param name
     * @return
     */
    @Select("select count(*) from category where name = #{name}")
    Integer countByName(String name);

    /**
     * 查询所有分类
     * @return
     */
    @Select("select * from category order by sort asc, create_time desc")
    List<Category> getAll();

}
