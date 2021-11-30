package com.ctplus.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ctplus.common.CTPFuture;
import com.ctplus.service.StrategyService;
import com.ctplus.util.RedisUtil;
import com.ctplus.vo.Json;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/strategy")
public class StrategyController {

    @Autowired
    private StrategyService strategyService;

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource
    private RedisUtil redisUtil;

    @RequestMapping("/queryTemplate")
    public Json queryTemplate(String name) throws ClassNotFoundException {

        Class strategyClass =Class.forName("com.ctplus.entity.strategyEntity."+name+"Entity");

        Field[] fields=strategyClass.getDeclaredFields();
        ArrayList<String> templateReponse = new ArrayList<>();
        for(Field field :fields){
            if(!field.getName().equals("log")){
                templateReponse.add(field.getName());
            }
        }

        return Json.succ("查询"+name+"策略模版",templateReponse);
    }

    @RequestMapping("/lookStrategy")
    public Json lookStrategyOnFuture(String code){
        CTPFuture ctpFuture=focusContainer.get(code);
        ArrayList<HashMap<String,String>> result = new ArrayList<>();

        return Json.succ("查看"+code+"期货所有策略",result);
    }

    @RequestMapping("/list")
    public Json listAllStrategies(){
        ArrayList<HashMap> result = strategyService.listAll();
        return Json.succ("查询所有策略",result);
    }

    @PostMapping("/changeRealTrade")
    public Json changeRealTrade(@RequestBody String body){
        JSONObject json= JSON.parseObject(body);
        String code=json.getString("code");
        String strategyName=json.getString("strategy");
        int switcher=json.getIntValue("realTrade");
        strategyService.changeRealTrade(code,strategyName,switcher);
        System.out.println("*********************************");
        System.out.println(code+"实盘状态变更:"+switcher+", "+strategyName);
        System.out.println("*********************************");
        return Json.succ(code+"实盘开关已改变");
    }


}
