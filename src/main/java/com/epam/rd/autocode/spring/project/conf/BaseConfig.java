package com.epam.rd.autocode.spring.project.conf;

import com.epam.rd.autocode.spring.project.mapper.BookMapper;
import com.epam.rd.autocode.spring.project.mapper.ClientMapper;
import com.epam.rd.autocode.spring.project.mapper.EmployeeMapper;
import com.epam.rd.autocode.spring.project.mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BaseConfig{
    @Bean
    public BookMapper bookMapper(){
        return Mappers.getMapper(BookMapper.class);
    }

    @Bean
    public ClientMapper clientMapper(){
        return Mappers.getMapper(ClientMapper.class);
    }

    @Bean
    public EmployeeMapper employeeMapper(){
        return Mappers.getMapper(EmployeeMapper.class);
    }

    @Bean
    public OrderMapper orderMapper(){
        return Mappers.getMapper(OrderMapper.class);
    }
}
