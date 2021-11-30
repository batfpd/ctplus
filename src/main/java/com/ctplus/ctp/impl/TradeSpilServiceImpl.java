package com.ctplus.ctp.impl;

import com.ctplus.ctp.inter.TradeSpilService;
import org.springframework.stereotype.Service;
import ctp.thostapi.*;

import javax.annotation.Resource;

@Service
public class TradeSpilServiceImpl implements TradeSpilService {

    @Resource
    private CThostFtdcTraderApi traderApi;

    final static String m_BrokerId = "20000";
    final static String m_UserId = "201001505";
    final static String m_InvestorId = "201001505";
    final static String m_PassWord = "qh120216";
    final static String m_TradingDay = "20210617";
    final static String m_AccountId = "201001505";
    final static String m_CurrencyId = "CNY";



    @Override
    public void login() {
        //初始化reqUserLoginField
        CThostFtdcReqUserLoginField reqUserLoginField = new CThostFtdcReqUserLoginField();
        reqUserLoginField.setBrokerID(m_BrokerId);
        reqUserLoginField.setUserID(m_UserId);
        reqUserLoginField.setPassword(m_PassWord);
        int login = traderApi.ReqUserLogin(reqUserLoginField, 0);
    }

    @Override
    public void qryTradingAccount() {
        CThostFtdcQryTradingAccountField qryTradingAccount = new CThostFtdcQryTradingAccountField();
        qryTradingAccount.setBrokerID(m_BrokerId);
        qryTradingAccount.setCurrencyID(m_CurrencyId);
        qryTradingAccount.setInvestorID(m_InvestorId);
        int query = traderApi.ReqQryTradingAccount(qryTradingAccount, 1);
    }

    @Override
    public void reqQryTrade() {
        CThostFtdcQryTradeField qryTradeField = new CThostFtdcQryTradeField();
        qryTradeField.setBrokerID(m_BrokerId);
        qryTradeField.setInvestorID(m_InvestorId);
        int query = traderApi.ReqQryTrade(qryTradeField, 1);
    }

    @Override
    public void reqQryInvestorPosition() {
        CThostFtdcQryInvestorPositionCombineDetailField  field = new CThostFtdcQryInvestorPositionCombineDetailField();
        field.setBrokerID(m_BrokerId);
        field.setInvestorID(m_InvestorId);
        traderApi.ReqQryInvestorPositionCombineDetail(field,1);
    }

    @Override
    public void reqOrderInsert() {
        CThostFtdcInputOrderField field = new CThostFtdcInputOrderField();
        field.setBrokerID(m_BrokerId);
        field.setInvestorID(m_InvestorId);

    }
}
