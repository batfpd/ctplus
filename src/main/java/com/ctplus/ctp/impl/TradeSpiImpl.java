package com.ctplus.ctp.impl;

import com.ctplus.common.CTPFuture;
import com.ctplus.ctp.inter.TraderSpi;
import com.ctplus.entity.OrderEntity;
import com.ctplus.service.MessageService;
import com.ctplus.service.RecordService;
import com.ctplus.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ctp.thostapi.*;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ctp.thostapi.thosttraderapiConstants.THOST_FTDC_OST_AllTraded;
import static ctp.thostapi.thosttraderapiConstants.THOST_FTDC_OST_Canceled;

@Component
public class TradeSpiImpl extends CThostFtdcTraderSpi implements TraderSpi {
    private static final Logger log = LoggerFactory.getLogger(TradeSpiImpl.class);

    @Resource
    CThostFtdcTraderApi traderApi;

    @Autowired
    MessageService messageService;

    @Autowired
    TradeSpilServiceImpl spilService;

    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;

    @Resource(name = "accountMap")
    private HashMap<String, Double> accountMap;

    @Resource(name = "allFutures")
    private ConcurrentHashMap<String, String> futureSet;

    @Autowired
    TradeService tradeService;

    @Autowired
    RecordService recordService;

    private ConcurrentHashMap<Integer, OrderEntity> tradeMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Boolean> tradeStatusMap = new ConcurrentHashMap<>();

    final static String m_BrokerId = "20000";
    final static String m_UserId = "201001505";
    final static String m_InvestorId = "201001505";
    final static String m_PassWord = "qh120216";
    final static String m_TradingDay = "20210622";
    final static String m_AccountId = "201001505";
    final static String m_CurrencyId = "CNY";
    final static String m_AppId = "client_zjds_1.0.0";
    final static String m_AuthCode = "ZWTSWN3Y51MWPYGM";

    public static Boolean loginStatus = false;

    public static boolean canNotify = true;

    @Override
    public void OnFrontConnected() {
        System.out.println("On Front Connected");
        CThostFtdcReqAuthenticateField field = new CThostFtdcReqAuthenticateField();
        field.setBrokerID(m_BrokerId);
        field.setUserID(m_UserId);
        field.setAppID(m_AppId);
        field.setAuthCode(m_AuthCode);
        traderApi.ReqAuthenticate(field, 0);
        System.out.println("Send ReqAuthenticate ok");
    }

    @Override
    public void OnRspAuthenticate(CThostFtdcRspAuthenticateField pRspAuthenticateField, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.printf("Login ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());

            return;
        }
        System.out.println("OnRspAuthenticate success!!!");
        CThostFtdcReqUserLoginField field = new CThostFtdcReqUserLoginField();
        field.setBrokerID(m_BrokerId);
        field.setUserID(m_UserId);
        field.setPassword(m_PassWord);
        traderApi.ReqUserLogin(field, 0);
        System.out.println("Send login ok");
    }

    @Override
    public void OnRspUserLogin(CThostFtdcRspUserLoginField pRspUserLogin, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            try {
                throw new Exception("????????????");
            } catch (Exception e) {
                System.out.println("???????????? error: " + pRspInfo.getErrorMsg());
            }
        } else {
            loginStatus = true;
            System.out.println("login success");
        }

    }

    @Override
    public Boolean getLoginStatus() {
        return loginStatus;
    }

    @Override
    public void OnRspQryDepthMarketData(CThostFtdcDepthMarketDataField pDepthMarketData, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pDepthMarketData != null) {
            System.out.printf(LocalDateTime.now() + " InstrumentID[%s]AskPrice1[%f]" +
                            "BidPrice1[%f]LastPrice[%f]UpdateTime[%s]\n",
                    pDepthMarketData.getInstrumentID(), pDepthMarketData.getAskPrice1(),
                    pDepthMarketData.getBidPrice1(), pDepthMarketData.getLastPrice(),
                    pDepthMarketData.getUpdateTime());
        } else {
            System.out.printf("NULL obj\n");
        }

    }


    @Override
    public void OnRspQryTradingAccount(CThostFtdcTradingAccountField pTradingAccount, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        System.out.println("+++++++++++ OnRspQryTradingAccount +++++++++++");
        System.out.println("????????????" + pTradingAccount.getAvailable());
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.println("???????????????   ErrorID: " + pRspInfo.getErrorID() + ", ErrorMsg: " + pRspInfo.getErrorMsg());
//            System.out.printf("OnRspQryTradingAccount ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());

            return;
        }
        if (pTradingAccount != null) {
            System.out.printf("Balance[%f]Available[%f]WithdrawQuota[%f]Credit[%f]\n",
                    pTradingAccount.getBalance(), pTradingAccount.getAvailable(), pTradingAccount.getWithdrawQuota(),
                    pTradingAccount.getCredit());
            accountMap.put("Available", pTradingAccount.getAvailable());//???Available??????
            if (pTradingAccount.getAvailable() < 500000 && canNotify) {
                System.out.println(" ????????????????????????");
                try {
                    LocalDateTime date = LocalDateTime.now();
                    String time = date.getYear() + (date.getMonthValue() < 10 ? "0" : "") + date.getMonthValue() + date.getDayOfMonth() + " " + date.getHour() + ":" + date.getMinute();
                    double real = pTradingAccount.getBalance() / 10000;
                    messageService.sendNotification(time, real + "");
                    canNotify = false;
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("==??????????????????==");
                }
            }
        } else {
            System.out.println(LocalDateTime.now() + " OnRspQryTradingAccount ????????????");
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++");
    }

    @Override
    public void OnRspQryInvestorPosition(CThostFtdcInvestorPositionField pPosition, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {


        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.printf("OnRspQryInvestorPosition ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());

            return;
        }

        if (pPosition != null) {
            System.out.println("=========????????????=========");
		 Class resp=CThostFtdcInvestorPositionField.class;
		 Method[] methods= resp.getMethods();
//		 for(Method method:methods){
//
//			 if(method.getName().startsWith("get")){
//				 System.out.print(method.getName()+": ");
//				 try {
//					 System.out.println(method.invoke(pPosition).toString());
//				 } catch (IllegalAccessException | InvocationTargetException e) {
//					 e.printStackTrace();
//				 }
//			 }
//		 }
            System.out.println("????????????: " + pPosition.getInstrumentID());
            System.out.println("????????????: " + pPosition.getPosition());
            System.out.println("????????????: " + pPosition.getPositionCost());
            System.out.println("????????????: " + pPosition.getOpenCost());
            System.out.println("????????????: " + pPosition.getPositionProfit());
            System.out.println("????????????: " + pPosition.getPosiDirection());
            System.out.println("=========================");

            for (Map.Entry<String, CTPFuture> entry : focusContainer.entrySet()) {
                if (entry.getKey().equals(pPosition.getInstrumentID())) {
                    if (pPosition.getPosition() > 0) {
                        System.out.println("TradeSpiImpl change future position");
                        if (pPosition.getPosiDirection() == '2') { // '2' ??????
                            entry.getValue().setTodayBuyPosition(pPosition.getTodayPosition());
                            entry.getValue().setYesterdayBuyPosition(pPosition.getPosition() - pPosition.getTodayPosition());
                        } else {
                            entry.getValue().setTodaySellPosition(pPosition.getTodayPosition());
                            entry.getValue().setYesterdaySellPosition(pPosition.getPosition() - pPosition.getTodayPosition());
                        }
                    }
                }
            }


        } else {
            System.out.println(LocalDateTime.now() + " OnRspQryInvestorPosition ????????????");
        }
    }

    @Override
    public void OnRspQryTrade(CThostFtdcTradeField pTrade, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        System.out.println("TTTTTTTTTTTTTTTTTTTT");
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.printf("OnRspQryTradingAccount ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());

            return;
        }

        if (pTrade != null) {
            System.out.printf("Balance[%f]Available[%f]WithdrawQuota[%f]Credit[%f]\n",
                    pTrade.getPrice() + pTrade.getTradeTime());
        }

    }

    @Override
    public void OnRspQryInvestorPositionCombineDetail(CThostFtdcInvestorPositionCombineDetailField postionCombineDetail, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.println("error");
            System.out.printf("OnRspQryTradingAccount ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());

            return;
        }

        if (postionCombineDetail != null) {

            System.out.printf(postionCombineDetail.getTradingDay());
        } else {
            System.out.printf("NULL obj\n");
        }

    }

    @Override
    synchronized public void OnRspQryInstrument(CThostFtdcInstrumentField pInstrument, CThostFtdcRspInfoField pRspInfo, int nRequestID, boolean bIsLast) {
        if (pRspInfo != null && pRspInfo.getErrorID() != 0) {
            System.out.printf("OnRspQryInstrument ErrorID[%d] ErrMsg[%s]\n", pRspInfo.getErrorID(), pRspInfo.getErrorMsg());
            return;
        }
        if (pInstrument != null) {
//            System.out.printf("%s\n",pInstrument.getInstrumentID());
            futureSet.put(pInstrument.getInstrumentID(), pInstrument.getExchangeID());
            if (focusContainer.containsKey(pInstrument.getInstrumentID())) {
                CTPFuture ctpFuture = focusContainer.get(pInstrument.getInstrumentID());
                ctpFuture.setExchange(pInstrument.getExchangeID());
                log.info("????????????????????????{}", ctpFuture.toString());
                log.info("????????????????????????{}", focusContainer.get(pInstrument.getInstrumentID()));
            }
        } else {
            System.out.printf("NULL obj\n");
        }
    }

    // ??????????????????
    @Override
    public void OnErrRtnOrderInsert(CThostFtdcInputOrderField cThostFtdcInputOrderField, CThostFtdcRspInfoField cThostFtdcRspInfoField) {
        System.out.println("**********OnErrRtnOrderInsert*********");
        System.out.println(cThostFtdcInputOrderField.getInstrumentID());
        System.out.println(cThostFtdcRspInfoField.getErrorMsg());
    }

    // ?????????????????? code:31
    // ???????????????????????? code:51
    @Override
    public void OnRspOrderInsert(CThostFtdcInputOrderField cThostFtdcInputOrderField, CThostFtdcRspInfoField cThostFtdcRspInfoField, int i, boolean b) {
        System.out.println("**********OnRspOrderInsert*********");
        System.out.println(cThostFtdcInputOrderField.getInstrumentID());
        System.out.println(cThostFtdcRspInfoField.getErrorMsg());
        System.out.println("ErrorCode: " + cThostFtdcRspInfoField.getErrorID());
    }

    // ?????????????????????
    @Override
    public void OnRtnOrder(CThostFtdcOrderField cThostFtdcOrderField) {
        System.out.println("**********OnRtnOrder*********");
        System.out.println("Instrument: " + cThostFtdcOrderField.getInstrumentID());
        System.out.println("ActiveTradeID: " + cThostFtdcOrderField.getActiveTraderID());
        System.out.println("OrderLocalID: " + cThostFtdcOrderField.getOrderLocalID());
        System.out.println("OrderSysID: " + cThostFtdcOrderField.getOrderSysID());
        System.out.println("SettlementID: " + cThostFtdcOrderField.getSettlementID());
        System.out.println("SequenceNo: " + cThostFtdcOrderField.getSequenceNo());
        System.out.println("OrderStatus: " + cThostFtdcOrderField.getOrderStatus());
        System.out.println("StatusMsg: " + cThostFtdcOrderField.getStatusMsg());
        System.out.println(LocalDateTime.now());
        int orderLocalID = Integer.parseInt(cThostFtdcOrderField.getOrderLocalID().trim());
        if (!tradeMap.containsKey(orderLocalID)) {
            OrderEntity orderEntity = convertOrderFieldToOrderEntity(cThostFtdcOrderField);
            tradeMap.put(orderLocalID, orderEntity);
            tradeStatusMap.put(orderLocalID, false);
        } else {
            if (cThostFtdcOrderField.getOrderStatus() == THOST_FTDC_OST_AllTraded) {
                tradeStatusMap.put(orderLocalID, true);
            }
            if (cThostFtdcOrderField.getOrderStatus() == THOST_FTDC_OST_Canceled) { // ??????
                // ????????????????????????
                CTPFuture ctpFuture = focusContainer.get(cThostFtdcOrderField.getInstrumentID());
                if (ctpFuture != null) {
                    if (cThostFtdcOrderField.getCombOffsetFlag().equals("0")) { // Open
                        if (cThostFtdcOrderField.getDirection() == '0') {   //Buy
                            ctpFuture.setTodayBuyPosition(ctpFuture.getTodayBuyPosition() - cThostFtdcOrderField.getVolumeTotal());
                        } else {
                            ctpFuture.setTodaySellPosition(ctpFuture.getTodaySellPosition() - cThostFtdcOrderField.getVolumeTotal());
                        }
                    } else if (cThostFtdcOrderField.getCombOffsetFlag().equals("1")) { // Close
                        // ??????????????????????????????????????????


                    } else if (cThostFtdcOrderField.getCombOffsetFlag().equals("3")) { // CloseToday
                        if (cThostFtdcOrderField.getDirection() == '0') {   //Buy
                            ctpFuture.setTodayBuyPosition(ctpFuture.getTodayBuyPosition() + cThostFtdcOrderField.getVolumeTotal());
                        } else {
                            ctpFuture.setTodaySellPosition(ctpFuture.getTodaySellPosition() + cThostFtdcOrderField.getVolumeTotal());
                        }
                    } else if (cThostFtdcOrderField.getCombOffsetFlag().equals("4")) { // CloseYesterday
                        if (cThostFtdcOrderField.getDirection() == '0') {   //Buy
                            ctpFuture.setTodayBuyPosition(ctpFuture.getYesterdayBuyPosition() + cThostFtdcOrderField.getVolumeTotal());
                        } else {
                            ctpFuture.setTodaySellPosition(ctpFuture.getYesterdaySellPosition() + cThostFtdcOrderField.getVolumeTotal());
                        }
                    }
                }
                String[] time = cThostFtdcOrderField.getInsertTime().split(":");
                int hour = Integer.parseInt(time[0]);
                int minute = Integer.parseInt(time[1]);
                if ((hour == 8 || hour == 21) && minute == 59) {
                    System.out.println("26:????????????????????????SHFE:??????????????????????????????");
                    tradeService.incompleteOrder.add(tradeMap.get(orderLocalID));
                }
                tradeMap.remove(orderLocalID);
                tradeStatusMap.remove(orderLocalID);
            }
        }

    }

    @Override
    public void OnRtnTrade(CThostFtdcTradeField cThostFtdcTradeField) {
        if (cThostFtdcTradeField != null) {
            System.out.println("**********OnRtnTrade*********");
            System.out.println("OrderLocalID: " + cThostFtdcTradeField.getOrderLocalID().trim());
            System.out.println("OrderSysID: " + cThostFtdcTradeField.getOrderSysID().trim());
            System.out.println("TradeID: " + cThostFtdcTradeField.getTradeID());
            System.out.println("Instrument: " + cThostFtdcTradeField.getInstrumentID());
            System.out.println("Pirce: " + cThostFtdcTradeField.getPrice());
            System.out.println("Volume: " + cThostFtdcTradeField.getVolume());
            System.out.println("TradeDate: " + cThostFtdcTradeField.getTradeDate());
            System.out.println("TradeTime: " + cThostFtdcTradeField.getTradeTime());
            System.out.println("SequenceNo: " + cThostFtdcTradeField.getSequenceNo());
            System.out.println("SettlementID: " + cThostFtdcTradeField.getSettlementID());
            int orderLocalID = Integer.parseInt(cThostFtdcTradeField.getOrderLocalID().trim());
            System.out.println("~~:" + orderLocalID);
            if (tradeStatusMap.get(orderLocalID)) {
                OrderEntity trade = addTradeFieldToOrderEntity(tradeMap.get(orderLocalID), cThostFtdcTradeField);
                recordService.insert(trade);
                System.out.println(orderLocalID + "????????????????????????!");
                tradeMap.remove(orderLocalID);
                tradeStatusMap.remove(orderLocalID);
            }

        }


    }

    public OrderEntity convertOrderFieldToOrderEntity(CThostFtdcOrderField cThostFtdcOrderField) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setFrontID(cThostFtdcOrderField.getFrontID());
        orderEntity.setSessionID(cThostFtdcOrderField.getSessionID());
        orderEntity.setOrderLocalID(Integer.parseInt(cThostFtdcOrderField.getOrderLocalID().trim()));
        orderEntity.setExchangeID(cThostFtdcOrderField.getExchangeID());
        orderEntity.setInstrument(cThostFtdcOrderField.getInstrumentID());
        orderEntity.setDirection(cThostFtdcOrderField.getDirection() + "");
        orderEntity.setVolume(cThostFtdcOrderField.getVolumeTotal());
        orderEntity.setOffsetFlag(cThostFtdcOrderField.getCombOffsetFlag());
        String note = cThostFtdcOrderField.getOrderRef().trim();
        String[] split = note.split(",");
        int orderRef = Integer.parseInt(split[0].trim());
        String strategyName;
        double idealPrice;
        if (split.length == 1) {
            System.out.println("null");
            strategyName = "manual";
            idealPrice = 0.0;
        } else {
            System.out.println("ok");
            strategyName = split[1];
            idealPrice = Double.parseDouble(split[2]);
        }
        orderEntity.setStrategy(strategyName);
        orderEntity.setIdealPrice(idealPrice);
        return orderEntity;
    }

    public OrderEntity addTradeFieldToOrderEntity(OrderEntity orderEntity, CThostFtdcTradeField cThostFtdcTradeField) {
        String note = cThostFtdcTradeField.getOrderRef();
        String[] split = note.split(",");
        int orderRef = Integer.parseInt(split[0].trim());
        String strategyName;
        double idealPrice;
        if (split.length == 1) {
            System.out.println("null");
            strategyName = "manual";
            idealPrice = 0.0;
        } else {
            System.out.println("ok");
            strategyName = split[1];
            idealPrice = Double.parseDouble(split[2]);
        }
//        String strategyName = split[1];
//        double idealPrice = Double.parseDouble(split[2]);
        orderEntity.setExchangeID(cThostFtdcTradeField.getExchangeID());
        orderEntity.setOrderLocalID(Integer.parseInt(cThostFtdcTradeField.getOrderLocalID().trim()));
        orderEntity.setOrderSysID(Integer.parseInt(cThostFtdcTradeField.getOrderSysID().trim()));
        orderEntity.setOrderRef(orderRef);
        orderEntity.setInstrument(cThostFtdcTradeField.getInstrumentID());
        orderEntity.setDirection(cThostFtdcTradeField.getDirection() + "");
        orderEntity.setVolume(cThostFtdcTradeField.getVolume());
        orderEntity.setDealPrice(cThostFtdcTradeField.getPrice());
        orderEntity.setIdealPrice(idealPrice);
        orderEntity.setTradeDate(cThostFtdcTradeField.getTradeDate());
        orderEntity.setTradeTime(cThostFtdcTradeField.getTradeTime());
        orderEntity.setStrategy(strategyName);
        LocalDateTime now = LocalDateTime.now();
        Timestamp ts = Timestamp.valueOf(now);
        orderEntity.setTimest(ts);
        return orderEntity;
    }
}

