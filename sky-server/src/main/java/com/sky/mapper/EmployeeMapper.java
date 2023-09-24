package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username=#{username}")
    Employee getByUsername(String username);


    @Insert("insert into employee (name,username,password,phone,sex,idNumber,createTime,updateTime,createUser,updateUser)" +
            "values (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);
//最新javaweb课程的08-15讲了如何设置注解内SQL语句的自动补全，可以看看
}
