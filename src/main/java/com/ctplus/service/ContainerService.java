package com.ctplus.service;

import com.ctplus.common.CTPFuture;
import com.ctplus.common.StrategyObserver;
import com.ctplus.util.RedisUtil;
import ctp.thostapi.CThostFtdcMdApi;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ContainerService {


    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;


    @Resource
    private RedisUtil redisUtil;

    @Resource
    CThostFtdcMdApi mdApi;

    /**
     * 将某一期货添加到关注列表中，并将关注列表更新到Redis中
     * @param ctpFuture
     * @return
     */
    public boolean addFocusFuture(CTPFuture ctpFuture){
        CTPFuture tmp=focusContainer.put(ctpFuture.getCode(),ctpFuture);
        boolean b= tmp == null;
        String[] instrument= new String[]{ctpFuture.getCode()};
        boolean d=mdApi.SubscribeMarketData(instrument,1)==0;
        boolean c=redisUtil.hset("focusFuture",ctpFuture.getCode(),ctpFuture);
        if(b&&c&&d){
            System.out.println("期货"+ctpFuture.getCode()+"已添加至关注Container中！");
        }else{
            System.out.println("期货"+ctpFuture.getCode()+"添加至关注列表失败！");
        }
        return b&&c&&d;
    }

    public List<String> showAllCTPFutures(){
        List<String> list =new ArrayList<String>();
        for(Map.Entry<String,CTPFuture> entry:focusContainer.entrySet()){
            list.add(entry.getKey());
        }
        return list;
    }

    /**
     * 列出所有策略
     * @return
     */
    public List<String> showAllStrategies(){
        List<String> list =new ArrayList<String>();
        list.add("Dingshi2");
        list.add("Dingshi4");
        list.add("ExampleStrategy");
        return list;
    }

    public boolean expireFuture(String oldCode, String newCode){
        CTPFuture oldFuture = focusContainer.get(oldCode);
        CTPFuture newFuture = focusContainer.get(newCode);
        for(Map.Entry<String, StrategyObserver> entry:oldFuture.getObserverMap().entrySet()){
            StrategyObserver observer=entry.getValue();
            StrategyObserver newObserver = observer.clone(newCode);
            newFuture.attach(newObserver);
        }
        if(oldFuture.getObserverMap().size()==newFuture.getObserverMap().size()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 将某一期货从关注列表中删除
     * @param code
     * @return
     */
    public boolean deleteFuture(String code){
        CTPFuture toBeRemovedFuture=focusContainer.get(code);
        boolean localDelete=focusContainer.remove(code,toBeRemovedFuture);
        boolean redisDelete=redisUtil.hdel("focusFuture",toBeRemovedFuture.getCode())==1;
        redisUtil.del(code+"strategies");
        return localDelete&&redisDelete;

    }



}
