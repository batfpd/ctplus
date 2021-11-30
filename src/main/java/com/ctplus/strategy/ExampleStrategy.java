package com.ctplus.strategy;

import com.ctplus.common.StrategyObserver;
import com.ctplus.entity.strategyEntity.ExampleStrategyEntity;
import com.ctplus.service.TradeService;
import com.ctplus.util.RedisUtil;
import com.ctplus.util.SpringContextUtil;
import org.springframework.stereotype.Component;

@Component
public class ExampleStrategy extends StrategyObserver {

    private double weekH;  //
    private double weekL;

    private int ourUnit;

    private TradeService tradeService;
    private RedisUtil redisUtil;

    public ExampleStrategy(){
        System.out.println("dingshi3 无参构造");
    }

    public ExampleStrategy(Object dingshi3EntityObj){
        ExampleStrategyEntity dingshi3Entity = (ExampleStrategyEntity) dingshi3EntityObj;
        super.name= dingshi3Entity.getName();
        super.code=dingshi3Entity.getCode();
        weekH=dingshi3Entity.getWeekH();
        weekL=dingshi3Entity.getWeekL();
        ourUnit=dingshi3Entity.getOurUnit();
        super.direction=dingshi3Entity.getOurMarket();
        realTrade=dingshi3Entity.getRealTrade();
        if(tradeService==null){
            tradeService= SpringContextUtil.getBean(TradeService.class);
        }

        if(redisUtil==null){
            redisUtil = SpringContextUtil.getBean(RedisUtil.class);
        }

    }
    public ExampleStrategy(ExampleStrategyEntity dingshi3Entity){
        super.name= dingshi3Entity.getName();
        super.code=dingshi3Entity.getCode();
        weekH=dingshi3Entity.getWeekH();
        weekL=dingshi3Entity.getWeekL();
        ourUnit=dingshi3Entity.getOurUnit();
        super.direction=dingshi3Entity.getOurMarket();
        realTrade=dingshi3Entity.getRealTrade();
        if(tradeService==null){
            tradeService= SpringContextUtil.getBean(TradeService.class);
        }

        if(redisUtil==null){
            redisUtil = SpringContextUtil.getBean(RedisUtil.class);
        }

    }

    @Override
    public void update(double price, String ts) {

                if(realTrade>0){
                    tradeService.buyOpenPairLock(super.code,price,1,super.name);
                }

    }

    private void updateRedis(){
        ExampleStrategyEntity entity=new ExampleStrategyEntity();
        entity.setName(super.name);
        entity.setCode(super.code);
        entity.setWeekH(weekH);
        entity.setWeekL(weekL);
        entity.setOurUnit(ourUnit);
        entity.setOurMarket(super.direction);
        entity.setRealTrade(realTrade);
        redisUtil.hset(super.code+"-strategies",super.name,entity);
    }

    @Override
    public StrategyObserver clone(String newCode) {
        ExampleStrategyEntity entity = new ExampleStrategyEntity();
        String strategyName;
        if(super.name.contains(code)){
            strategyName = super.name.replaceFirst(super.code, newCode);
        }else{
            strategyName=super.name+"-"+newCode;
        }
        entity.setName(strategyName);
        entity.setCode(newCode);
        entity.setWeekH(weekH);
        entity.setWeekL(weekL);
        entity.setOurUnit(ourUnit);
        entity.setOurMarket(super.direction);
        entity.setRealTrade(realTrade);
        redisUtil.hset(newCode+"-strategies",strategyName,entity);
        return new ExampleStrategy(entity);
    }

    @Override
    public String toString() {
        return "ExampleStrategy{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", direction=" + direction +
                ", weekH=" + weekH +
                ", weekL=" + weekL +
                ", ourUnit=" + ourUnit +
                ", realTrade=" + realTrade +
                '}';
    }
}
