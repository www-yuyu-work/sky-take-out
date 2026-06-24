package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 统计今日新增用户数
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(*) from user where create_time between #{begin} and #{end}")
    Integer countNewUsers(LocalDateTime begin, LocalDateTime end);

    /**
     * 每日新增用户
     */
    @Select("select date_format(create_time, '%Y-%m-%d') as userDate, count(*) as newUsers " +
            "from user where create_time between #{begin} and #{end} " +
            "group by date_format(create_time, '%Y-%m-%d') order by userDate")
    List<Map<String, Object>> getDailyNewUsers(LocalDateTime begin, LocalDateTime end);

    /**
     * 截至每日的总用户量
     * 通过逐日累加实现
     */
    @Select("select count(*) from user where create_time &lt;= #{dayEnd}")
    Integer countTotalUsersByDay(LocalDateTime dayEnd);

    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

}
