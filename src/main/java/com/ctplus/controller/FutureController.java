package com.ctplus.controller;

import com.ctplus.common.CTPFuture;
import com.ctplus.service.StrategyService;
import com.ctplus.util.RedisUtil;
import com.ctplus.vo.Json;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;

@RestController
@RequestMapping("/future")
public class FutureController {

    @Autowired
    private StrategyService strategyService;

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource
    private RedisUtil redisUtil;

    @PostMapping("/addStrategy")
    public Json addStrategyToFuture(@RequestBody String body) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        JSONObject json= JSON.parseObject(body);
        String strategyClass=json.getString("strategyClass");
        Class strategyEntityClass = null;
        Object obj=null;
        try {
            strategyEntityClass = Class.forName("com.ctplus.entity.strategyEntity."+strategyClass+"Entity");
            Constructor constructor = strategyEntityClass.getConstructor();
            obj=constructor.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        HashMap<String,String> tempStore=new HashMap<>();
        assert strategyEntityClass != null;
        Field[] fields=strategyEntityClass.getDeclaredFields();
        Method[] methods=strategyEntityClass.getDeclaredMethods();
        for(Method method:methods){
            for(Field field :fields){
                if(method.getName().toLowerCase(Locale.ROOT).equals("set"+field.getName().toLowerCase(Locale.ROOT))){
                    Class clazz=field.getType();
                    if(clazz.equals(int.class)){
                        method.invoke(obj, json.getIntValue(field.getName()));
                        tempStore.put(field.getName(), json.getString(field.getName()));
                    }else if(clazz==double.class){
                        method.invoke(obj,  json.getDoubleValue(field.getName()));
                        tempStore.put(field.getName(), json.getString(field.getName()));
                    }else if(clazz==String.class){
                        method.invoke(obj,  json.getString(field.getName()));
                        tempStore.put(field.getName(), json.getString(field.getName()));
                    }

                }
            }
        }


        boolean succ=strategyService.addStrategyToFuture(json.getString("code"),strategyClass,obj);
        if(succ){
            return Json.succ(json.getString("code")+"期货添加策略成功！");
        }else {
            return Json.fail(json.getString("code")+"期货添加策略失败！");
        }

    }

    @PostMapping("/removeStrategy")
    public Json removeStrategyOnFuture(@RequestBody String body){
        JSONObject json= JSON.parseObject(body);
        String toBeModifiedCtpFutureCode=json.getString("code");
        String toBeRemovedStrategy=json.getString("toBeRemovedStrategy");
        boolean succ=strategyService.removeStrategyOnFuture(toBeModifiedCtpFutureCode,toBeRemovedStrategy);
        if(succ){
            return Json.succ(json.getString("code")+"期货删除策略成功！");
        }else {
            return Json.fail(json.getString("code")+"期货删除策略失败！");
        }
    }

    @PostMapping("/pairlock")
    public Json pairLockSwitch(@RequestBody String body){
        JSONObject json= JSON.parseObject(body);
        String code=json.getString("code");
        int switcher=json.getIntValue("pairlock");
        strategyService.changePairLock(code,switcher);
        System.out.println(code+"对锁状态变更:"+switcher);
        return Json.succ(code+"对锁开关已改变");
    }


}
