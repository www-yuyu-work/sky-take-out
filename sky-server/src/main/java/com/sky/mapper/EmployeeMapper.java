package com.sky.mapper;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);

    /**
     * 更新员工信息
     * @param employee
     */
    void update(Employee employee);

    /**
     * 员工分页查询（动态条件：姓名模糊搜索）
     * @param employeePageQueryDTO
     * @return
     */
    List<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 新增员工
     * @param employee
     */
    void insert(Employee employee);

}
