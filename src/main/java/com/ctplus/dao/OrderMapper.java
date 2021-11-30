package com.ctplus.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.ctplus.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {

    List<OrderEntity> getAll();

    List<OrderEntity> getOrderByStrategyAndTime(@Param("strategyName") String strategyName, @Param("startDate")String startTime, @Param("endDate") String endTime);

    void save(@Param("trade") OrderEntity trade);

}
