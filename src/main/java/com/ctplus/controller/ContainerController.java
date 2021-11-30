package com.ctplus.controller;

import com.ctplus.common.CTPFuture;
import com.ctplus.service.ContainerService;
import com.ctplus.vo.Json;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/container")
public class ContainerController {
    private static final Logger log = LoggerFactory.getLogger(ContainerController.class);

    @Resource(name = "focusContainer")
    private HashMap<String,CTPFuture> focusContainer;

    @Autowired
    private ContainerService containerService;


    @Resource(name = "allFutures")
    private ConcurrentHashMap<String, String> futureSet;

    @RequestMapping("/list")
    public Json listFutures(){
        ArrayList<HashMap<String,String>> result=new ArrayList<>();
        for(Map.Entry<String,CTPFuture> entry:focusContainer.entrySet()){

            log.info("ContainerController list focusContainer: {}",entry.getValue().toString());
            HashMap<String,String> map = new HashMap<>();
            CTPFuture ctpFuture=entry.getValue();
            map.put("fname",ctpFuture.getCode());
            map.put("fexchange",ctpFuture.getExchange());
            map.put("isPairLock",ctpFuture.getPairLock()+"");
            result.add(map);
        }
        return Json.succ("显示所有期货品种", result);
    }

    @RequestMapping("/listAllStrategiesInSystem")
    public Json listStrategies(){return Json.succ("显示所有策略模版",containerService.showAllStrategies());}

    @PostMapping("/addFuture")
    public Json addFutureToFocusContainer(@RequestBody String body) {
        JSONObject json= JSON.parseObject(body);
        String ctpFutureCode=json.getString("code");
        if(!futureSet.containsKey(ctpFutureCode)){
            return Json.fail("无"+ctpFutureCode+"期货");
        }
        double unitPrice=json.getDoubleValue("unitPrice");
        CTPFuture ctpFuture = new CTPFuture(ctpFutureCode, unitPrice);
        ctpFuture.setExchange(futureSet.get(ctpFutureCode));
        if(containerService.addFocusFuture(ctpFuture)){
            return Json.succ(ctpFutureCode+" 已添加到关注列表中");
        }else{
            return Json.fail(ctpFutureCode+" 添加失败");
        }
    }

    @PostMapping("/expireFuture")
    public Json expireFuture(@RequestBody String body) {
        JSONObject json= JSON.parseObject(body);
        String oldCode=json.getString("oldCode");
        String newCode = json.getString("newCode");
        if(!futureSet.containsKey(oldCode)){
            return Json.fail("无"+oldCode+"期货");
        }
        double unitPrice=json.getDoubleValue("unitPrice");
        CTPFuture ctpFuture = new CTPFuture(newCode, unitPrice);
        ctpFuture.setExchange(futureSet.get(newCode));
        boolean b=containerService.addFocusFuture(ctpFuture);
        boolean c=containerService.expireFuture(oldCode,newCode);
        if(b&&c){
            return Json.succ("变更期货成功");
        }else{
            return Json.fail("变更期货失败");
        }
    }

    // 删除一支期货
    @PostMapping("/deleteFuture")
    public Json deleteFuture(@RequestBody String body){
        JSONObject json= JSON.parseObject(body);
        String ctpFutureCode=json.getString("code");
        boolean succ= containerService.deleteFuture(ctpFutureCode);
        if(succ){
            return Json.succ(ctpFutureCode+"期货删除成功");
        }else{
            return Json.fail(ctpFutureCode+"期货删除失败");
        }
    }



}
