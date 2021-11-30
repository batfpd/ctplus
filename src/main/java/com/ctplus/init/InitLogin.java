package com.ctplus.init;

import com.ctplus.common.CTPFuture;
import com.ctplus.common.Entity;
import com.ctplus.common.StrategyObserver;
import com.ctplus.strategy.ExampleStrategy;
import com.ctplus.util.RedisUtil;
import ctp.thostapi.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ctp.thostapi.THOST_TE_RESUME_TYPE.THOST_TERT_RESTART;

@Component
@Order(2)
public class InitLogin implements ApplicationRunner {

    final static String m_BrokerId = "20000";
    final static String m_UserId = "201001505";
    final static String m_InvestorId = "201001505";
    final static String m_PassWord = "qh120216";
    final static String m_TradingDay = "20210605";
    final static String m_AccountId = "201001505";
    final static String m_CurrencyId = "CNY";
    final static String m_AppId = "client_zjds_1.0.0";
    final static String m_AuthCode = "ZWTSWN3Y51MWPYGM";
    final static String ctp1_MdAddress = "tcp://140.206.102.109:41253";
    final static String ctp1_TradeAddress = "tcp://140.206.102.109:41255";
    static Vector<String> instr_vec = new Vector<String>();

    @Resource
    private RedisUtil redisUtil;

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource(name = "refMap")
    private ConcurrentHashMap<String,Integer> refMap;

    @Resource
    CThostFtdcTraderApi  traderApi;

    @Resource
    CThostFtdcTraderSpi cThostFtdcTraderSpi;

    @Resource
    CThostFtdcMdApi  mdApi;

    @Resource
    CThostFtdcMdSpi cThostFtdcMdSpi;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        addToRedis();
        if(!setContainer()){
            System.out.println("量化交易系统初始化失败，请检查系统设置");
            return;
        }
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...20%");
        traderApi.RegisterSpi(cThostFtdcTraderSpi);
        traderApi.SubscribePrivateTopic(THOST_TERT_RESTART);
        traderApi.SubscribePublicTopic(THOST_TERT_RESTART);
        traderApi.RegisterFront(ctp1_TradeAddress);
        traderApi.Init();
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...30%");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...40%");
        // 查询余额
//        CThostFtdcQryTradingAccountField accountField=new CThostFtdcQryTradingAccountField();
//        accountField.setBrokerID(m_BrokerId);
//        accountField.setInvestorID(m_InvestorId);
//        accountField.setCurrencyID(m_CurrencyId);
//        traderApi.ReqQryTradingAccount(accountField,7);
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...50%");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询持仓
//        CThostFtdcQryInvestorPositionField positionField=new CThostFtdcQryInvestorPositionField();
//		positionField.setBrokerID(m_BrokerId);
//		positionField.setInvestorID(m_InvestorId);
//		traderApi.ReqQryInvestorPosition(positionField,1);
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...60%");
		// 订阅行情
        mdApi.RegisterSpi(cThostFtdcMdSpi);
        mdApi.RegisterFront(ctp1_MdAddress);
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...70%");
        String[] instrument=new String[1];
        for(Map.Entry<String,CTPFuture> entry:focusContainer.entrySet()){
            instrument[0]=entry.getKey();
            mdApi.SubscribeMarketData(instrument,0);
        }
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...80%");
        mdApi.Init();
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...90%");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询交易所所有期货
        CThostFtdcQryInstrumentField instrumentField=new CThostFtdcQryInstrumentField();
        traderApi.ReqQryInstrument(instrumentField,6);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(LocalDateTime.now()+" 量化交易系统初始化中...100%");


    }

    /**
     * 将Redis中的数据读入系统
     * @return
     */
    private boolean setContainer() {
        Map<Object, Object> futureMap= redisUtil.hmget("focusFuture");
        for(Map.Entry entry:futureMap.entrySet()){
            HashMap<String,String> map=(HashMap<String, String>) entry.getValue();
            CTPFuture futureInRedis=new CTPFuture(map.get("code"),Double.parseDouble(map.get("unitPrice")));
            futureInRedis.setExchange(map.get("exchange"));
            futureInRedis.setYesterdayBuyPosition(Integer.parseInt(map.get("yesterdayBuyPosition")));
            futureInRedis.setTodayBuyPosition(Integer.parseInt(map.get("todayBuyPosition")));
            futureInRedis.setYesterdaySellPosition(Integer.parseInt(map.get("yesterdaySellPosition")));
            futureInRedis.setTodaySellPosition(Integer.parseInt(map.get("todaySellPosition")));
            futureInRedis.setPairLock(Integer.parseInt(map.getOrDefault("pairLock", "-1")));
            Map<Object,Object> strategies=redisUtil.hmget(futureInRedis.getCode()+"-strategies");
            for(Map.Entry strategyEntry:strategies.entrySet()){
                StrategyObserver strategyObserver=null;
                Entity entity=(Entity) strategyEntry.getValue();
                if(entity.entity.equals("Dingshi2Entity")){
//                    Dingshi2Entity ds2entity = (Dingshi2Entity) strategyEntry.getValue();
//                    strategyObserver=new Dingshi2(ds2entity);
//                    strategyObserver=new Dingshi2(strategyEntry.getValue());
                }else if(entity.entity.equals("Dingshi4Entity")){
//                    Dingshi4Entity ds4entity = (Dingshi4Entity) strategyEntry.getValue();
//                    strategyObserver=new Dingshi4(ds4entity);
//                    strategyObserver=new Dingshi4(strategyEntry.getValue());
                }else if(entity.entity.equals("ExampleStrategyEntity")){
//                    ExampleStrategyEntity ds3entity = (ExampleStrategyEntity) strategyEntry.getValue();
//                    strategyObserver=new ExampleStrategy(ds3entity);
                    strategyObserver=new ExampleStrategy(strategyEntry.getValue());
                }
                futureInRedis.addStrategy(strategyObserver);
            }
            focusContainer.put(futureInRedis.getCode(),futureInRedis);
        }
        // OrderRef读入
        int requestID = (int) redisUtil.get("requestID");
        refMap.put("requestID",requestID);
        return focusContainer.size()==futureMap.size();
    }




    private void addToRedis(){
//        focusContainer.put("ag2112",new CTPFuture("ag2112",5000));
//        focusContainer.put("bu2109",new CTPFuture("bu2109",3400));

        for(Map.Entry<String, CTPFuture> entry:focusContainer.entrySet()){
            boolean redisSaved=redisUtil.hset("focusFuture",entry.getKey(),entry.getValue());
            System.out.println(entry.getKey()+"放入Redis成功"+redisSaved+"，此次放入1个数据！");
        }
    }
}

