package com.ctplus.entity.strategyEntity;

import com.ctplus.common.Entity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Slf4j
public class ExampleStrategyEntity extends Entity implements Serializable {


    @NotNull
    private String name;
    @NotNull
    private String code;

    @NotNull
    private int enterUnit;

    @NotNull
    private double winStop;
    @NotNull
    private double lossStop;

    @NotNull
    private double weekH;
    @NotNull
    private double weekL;

    @NotNull
    private int ourUnit;
    @NotNull
    private double ourPoint; // 基准价格
    @NotNull
    private int ourMarket;
    @NotNull
    private int realTrade;


    public ExampleStrategyEntity(){
        setEntity("ExampleStrategyEntity");
    }




}
