package com.ctplus.service;

import com.ctplus.common.CTPFuture;
import com.ctplus.common.Entity;
import com.ctplus.common.StrategyObserver;
import com.ctplus.entity.strategyEntity.ExampleStrategyEntity;
import com.ctplus.strategy.ExampleStrategy;
import com.ctplus.util.RedisUtil;
import com.ctplus.util.SpringContextUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 策略服务
 */
@Service
public class StrategyService {

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource
    private RedisUtil redisUtil;

    /**
     * 生成StrategyObserver实例，以供添加到期货到策略集合中
     * @param strategyClassName
     * @param args
     * @return
     */
    public StrategyObserver generateStrategy(String strategyClassName, Object args) {
        Class strategyClass = null;
        Object obj = null;
        try {
//            strategyClass= Class.forName(strategyClassName);
//            strategyClass= CaotpApplication.getRun().getType(strategyClassName).getClass();
            System.out.println(strategyClassName);
            if (strategyClassName.endsWith("ExampleStrategy")){
                strategyClass = SpringContextUtil.getBean(ExampleStrategy.class).getClass();
            }
//            if (strategyClassName.endsWith("Dingshi2")){
//                strategyClass = SpringContextUtil.getBean(Dingshi2.class).getClass();
//            }else if (strategyClassName.endsWith("Dingshi4")){
//                strategyClass = SpringContextUtil.getBean(Dingshi4.class).getClass();
//            }

            Constructor constructor = strategyClass.getConstructor(Object.class);
            obj = constructor.newInstance(args);

        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();

        }

        if (strategyClass.getSuperclass().equals(StrategyObserver.class)) {
            System.out.println(strategyClassName + "策略已生成, 参数: " + args.toString());
            return (StrategyObserver) obj;
        } else {
            System.out.println(strategyClassName + "策略生成失败");
            return null;
        }

    }


    /**
     * 将某一策略添加到某一支期货上
     * @param code 期货品种
     * @param strategyClassName 策略类的名称
     * @param entity 策略对应的Entity实体
     * @return
     */
    public boolean addStrategyToFuture(String code, String strategyClassName, Object entity) {
        StrategyObserver observer = generateStrategy(strategyClassName, entity);
        CTPFuture ctpFuture = focusContainer.get(code);
        ctpFuture.addStrategy(observer);
        boolean c = redisUtil.hset(code + "-strategies", observer.getName(), entity);
        return c;
    }

    /**
     * 将某一策略从某一期货上移除
     * @param code 期货品种
     * @param strategyName 策略名称
     * @return
     */
    public boolean removeStrategyOnFuture(String code, String strategyName) {
        CTPFuture ctpFuture = focusContainer.get(code);
        ctpFuture.deleteStrategy(strategyName);
        boolean c = redisUtil.hdel(code + "-strategies", strategyName) == 1;
        return c;
    }

    /**
     * 改变某一支期货的对锁要求
     * @param code
     * @param switcher
     */
    public void changePairLock(String code, int switcher) {
        CTPFuture ctpFuture = focusContainer.get(code);
        ctpFuture.setPairLock(switcher);
        boolean c=redisUtil.hset("focusFuture",ctpFuture.getCode(),ctpFuture);
        if(c){
            System.out.println("******************************");
            System.out.println(code+"对锁状态改变， strategyService");
            System.out.println("******************************");
        }
    }

    /**
     * 实盘开关
     * @param code
     * @param strategyName
     * @param switcher
     */
    public void changeRealTrade(String code, String strategyName, int switcher) {
        CTPFuture ctpFuture = focusContainer.get(code);
        StrategyObserver observer=ctpFuture.getObserverMap().get(strategyName);
        observer.setRealTrade(switcher);
        Entity entity= (Entity) redisUtil.hget(code+"-strategies", strategyName);
        if(entity.entity.equals("ExampleStrategyEntity")){
            ExampleStrategyEntity ds3entity = (ExampleStrategyEntity) entity;
            ds3entity.setRealTrade(switcher);
            redisUtil.hset(code+"-strategies",strategyName,ds3entity);
        }

    }

    /**
     * 列出当前系统下所有期货的所有策略
     * @return
     */
    public ArrayList<HashMap> listAll() {
        ArrayList<HashMap> result = new ArrayList<>();
        for (Map.Entry<String, CTPFuture> entry : focusContainer.entrySet()) {
            CTPFuture ctpFuture = entry.getValue();
            for (Map.Entry<String, StrategyObserver> observerEntry : ctpFuture.getObserverMap().entrySet()) {
                HashMap<String, String> map = new HashMap<>();
                StrategyObserver observer = observerEntry.getValue();
                map.put("sname", observer.getName());
                map.put("scode", observer.getCode());
                map.put("sdirection", String.valueOf(observer.getDirection()));
                map.put("senterUnit", String.valueOf(observer.getEnterNo()));
                map.put("sRealTrade", String.valueOf(observer.getRealTrade()));
                map.put("status",observer.getStatus());

                result.add(map);
            }
        }
        return result;
    }


}
