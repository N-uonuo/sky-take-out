package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @return
     */
    @Select("select * from sky_take_out.employee where username=#{username}")
    Employee getByUsername(String username);


    @Insert("insert into sky_take_out.employee (name,username,password,phone,sex,idNumber,createTime,updateTime,createUser,updateUser)" +
            "values (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    void insert(Employee employee);


    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

}
//最新javaweb课程的08-15讲了如何设置注解内SQL语句的自动补全，可以看看