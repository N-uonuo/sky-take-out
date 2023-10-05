package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
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
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);


    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);


    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);

    //根据id查询员工
    @Select("select * from sky_take_out.employee where id=#{id}")
    Employee getById(Long id);
}
//最新javaWeb课程的08-15讲了如何设置注解内SQL语句的自动补全，可以看看