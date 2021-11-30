package com.ctplus.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@TableName("TradeRecord")
@Getter
@Setter
public class OrderEntity {

    @TableId(type = IdType.AUTO)
    private int id;
    @TableField("ExchangeID")
    private String ExchangeID;
    @TableField("OrderLocalID")
    private int OrderLocalID;
    @TableField("OrderSysID")
    private int OrderSysID;
    @TableField("FrontID")
    private int FrontID;
    @TableField("SessionID")
    private int SessionID;
    @TableField("OrderRef")
    private int OrderRef;
    @TableField("Instrument")
    private String Instrument;
    @TableField("Direction")
    private String Direction;
    @TableField("OffsetFlag")
    private String OffsetFlag;
    @TableField("Volume")
    private int Volume;
    @TableField("DealPrice")
    private double DealPrice;
    @TableField("IdealPrice")
    private double IdealPrice;
    @TableField("TradeDate")
    private String TradeDate;
    @TableField("TradeTime")
    private String TradeTime;
    @TableField("Strategy")
    private String Strategy;
    @TableField("Timest")
    private Timestamp Timest;
}
