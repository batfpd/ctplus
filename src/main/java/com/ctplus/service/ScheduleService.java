package com.ctplus.service;

import com.ctplus.common.CTPFuture;
import com.ctplus.ctp.impl.TradeSpiImpl;
import ctp.thostapi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static ctp.thostapi.THOST_TE_RESUME_TYPE.THOST_TERT_RESTART;

/**
 * 定时任务服务
 */
@Service
@EnableScheduling
@Order(3)
public class ScheduleService {


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


    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource
    private CThostFtdcTraderApi traderApi;

    @Autowired
    private TradeService tradeService;
    @Resource
    CThostFtdcTraderSpi cThostFtdcTraderSpi;

    @Resource
    CThostFtdcMdApi  mdApi;

    @Resource
    CThostFtdcMdSpi cThostFtdcMdSpi;

    private int cnt=0;

    /**
     * 每十秒轮询一次账户
     */
    @Scheduled(fixedRate = 10000) //十秒轮询一次
    public void queryAccount(){
        if(cnt==0){
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (Exception e) {
                e.printStackTrace();
            }
            cnt++;
        }
        System.out.println("TraderApi 已登陆: "+TradeSpiImpl.loginStatus);
        System.out.println("获取交易日: "+traderApi.GetTradingDay());
        CThostFtdcQryTradingAccountField qryTradingAccount = new CThostFtdcQryTradingAccountField();
        qryTradingAccount.setBrokerID(m_BrokerId);
        qryTradingAccount.setCurrencyID(m_CurrencyId);
        qryTradingAccount.setInvestorID(m_InvestorId);
        int statusCode=traderApi.ReqQryTradingAccount(qryTradingAccount, 8);
        System.out.println(LocalDateTime.now()+" 查询账户余额请求已发送！ 状态码: "+statusCode);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CThostFtdcQryInvestorPositionField positionField=new CThostFtdcQryInvestorPositionField();
        positionField.setBrokerID(m_BrokerId);
        positionField.setInvestorID(m_InvestorId);
        int statusCode2=traderApi.ReqQryInvestorPosition(positionField,1);
        System.out.println(LocalDateTime.now()+" 查询持仓请求已发送！状态码: "+statusCode2);
    }


    /**
     * 查询某一期货品种的持仓
     * @param code
     */
    public void queryPosition(String code){
        CThostFtdcQryInvestorPositionField positionField=new CThostFtdcQryInvestorPositionField();
        positionField.setBrokerID(m_BrokerId);
        positionField.setInvestorID(m_InvestorId);
        positionField.setInstrumentID(code);
        traderApi.ReqQryInvestorPosition(positionField,1);
    }

    /**
     * 开盘前一分钟的无效订单重新下单
     */
    @Scheduled(cron = "0 0 9,21 * * ?")
    public void completeEarlyOrder(){
        tradeService.completeEarlyOrder();
        tradeService.incompleteOrder=null;
    }

    /**
     * 定时登录
     */
    @Scheduled(cron = "0 50 8,20 * * ?")
    public void loginCTP(){
        traderApi.RegisterSpi(cThostFtdcTraderSpi);
        traderApi.SubscribePrivateTopic(THOST_TERT_RESTART);
        traderApi.SubscribePublicTopic(THOST_TERT_RESTART);
        traderApi.RegisterFront(ctp1_TradeAddress);
        traderApi.Init();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询余额
        CThostFtdcQryTradingAccountField accountField=new CThostFtdcQryTradingAccountField();
        accountField.setBrokerID(m_BrokerId);
        accountField.setInvestorID(m_InvestorId);
        accountField.setCurrencyID(m_CurrencyId);
        traderApi.ReqQryTradingAccount(accountField,1);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询持仓
        CThostFtdcQryInvestorPositionField positionField=new CThostFtdcQryInvestorPositionField();
        positionField.setBrokerID(m_BrokerId);
        positionField.setInvestorID(m_InvestorId);
        traderApi.ReqQryInvestorPosition(positionField,1);
        // 订阅行情
        mdApi.RegisterSpi(cThostFtdcMdSpi);
        mdApi.RegisterFront(ctp1_MdAddress);
        String[] instrument=new String[1];
        for(Map.Entry<String,CTPFuture> entry:focusContainer.entrySet()){
            instrument[0]=entry.getKey();
            mdApi.SubscribeMarketData(instrument,0);
        }
        mdApi.Init();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 查询交易所所有期货
        CThostFtdcQryInstrumentField instrumentField=new CThostFtdcQryInstrumentField();
        traderApi.ReqQryInstrument(instrumentField,1);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * 定时登出
     */
    @Scheduled(cron = "0 32 2 * * ?")
    public void logoutCTP(){
        CThostFtdcUserLogoutField logoutField = new CThostFtdcUserLogoutField();
        logoutField.setBrokerID(m_BrokerId);
        logoutField.setUserID(m_UserId);
        mdApi.ReqUserLogout(logoutField,1);
        traderApi.ReqUserLogout(logoutField,1);
        tradeService.incompleteOrder=null;
        cnt=0;
    }

    @Scheduled(cron = "0 0,30 8-15 * * ?")
    public void changeNotify(){
        TradeSpiImpl.canNotify=true;
    }



}
