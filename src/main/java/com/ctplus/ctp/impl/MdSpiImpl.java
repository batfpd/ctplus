package com.ctplus.ctp.impl;

import com.ctplus.common.CTPFuture;
import com.ctplus.ctp.inter.MdSpi;
import ctp.thostapi.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class MdSpiImpl extends CThostFtdcMdSpi implements MdSpi {

    @Resource
    CThostFtdcMdApi mdApi;

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    final static String m_BrokerId = "20000";
    final static String m_UserId = "201001505";
    final static String m_InvestorId = "201001505";
    final static String m_PassWord = "qh120216";
    final static String m_TradingDay = "20210617";
    final static String m_AccountId = "201001505";
    final static String m_CurrencyId = "CNY";

    @Override
    public void OnFrontConnected() {
        System.out.println("On Front Connected");
        CThostFtdcReqUserLoginField field = new CThostFtdcReqUserLoginField();
        field.setBrokerID(m_BrokerId);
        field.setUserID(m_UserId);
        field.setPassword(m_PassWord);
        mdApi.ReqUserLogin(field, 0);

    }

    @Override
    public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspUserLogin != null) {
            System.out.printf("Brokerid[%s]\n", pRspUserLogin.getBrokerID());
        }
        String[] instruementid = new String[1];

        for (Map.Entry<String, CTPFuture> entry : focusContainer.entrySet()) {
            instruementid[0] = entry.getKey();
            int resp = mdApi.SubscribeMarketData(instruementid, 1);
            if (resp == 1) {
                System.out.println(instruementid[0] + "订阅成功");

            }
        }

    }

    @Override
    public void OnRtnDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData) {
        if (pDepthMarketData != null) {
            System.out.printf(LocalDateTime.now() + " InstrumentID[%s]AskPrice1[%f]" +
                            "BidPrice1[%f]LastPrice[%f]UpdateTime[%s]\n",
                    pDepthMarketData.getInstrumentID(), pDepthMarketData.getAskPrice1(),
                    pDepthMarketData.getBidPrice1(), pDepthMarketData.getLastPrice(), pDepthMarketData.getUpdateTime());
            CTPFuture ctpFuture = focusContainer.get(pDepthMarketData.getInstrumentID());
            if (ctpFuture != null) {
                ctpFuture.change(pDepthMarketData.getLastPrice(), String.valueOf(System.currentTimeMillis()));
                if (ctpFuture.getExchange() == null) {
                    ctpFuture.setExchange(pDepthMarketData.getExchangeInstID());
                }
            }

        } else {
            System.out.printf("NULL obj\n");
        }
    }

    @Override
    public void OnRspError(CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        System.out.println("Error: " + cThostFtdcRspInfoField.getErrorMsg());
    }
}
