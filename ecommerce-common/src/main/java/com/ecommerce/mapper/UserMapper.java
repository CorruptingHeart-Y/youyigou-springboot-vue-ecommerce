package com.ecommerce.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) FROM e_user WHERE deleted = 0 AND role = 'USER'")
    Long countTotalUsers();

    @Select("SELECT COUNT(*) FROM e_user WHERE deleted = 0 AND role = 'USER' AND DATE(create_time) = CURDATE()")
    Long countTodayNewUsers();
}