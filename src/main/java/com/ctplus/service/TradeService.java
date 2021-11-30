package com.ctplus.service;

import com.ctplus.common.CTPFuture;
import com.ctplus.entity.OrderEntity;
import com.ctplus.util.RedisUtil;
import ctp.thostapi.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 交易服务
 */
@Service
@Getter
@Setter
public class TradeService {

    @Resource
    private CThostFtdcTraderApi traderApi;
    @Resource(name = "accountMap")
    private HashMap<String, Double> accountMap;
    @Resource(name = "focusContainer")
    private HashMap<String, CTPFuture> focusContainer;
    @Resource(name = "refMap")
    private ConcurrentHashMap<String, Integer> refMap;
    @Resource
    private RedisUtil redisUtil;

    private int yesterdaySellPosition;
    private int yesterdayBuyPosition;
    private int todaySellPosition;
    private int todayBuyPosition;
    private double availableMoney;
    private double unitPrice;
    private int pairLock;
    public ConcurrentLinkedDeque<OrderEntity> incompleteOrder;

    final static String m_BrokerId = "20000";
    final static String m_UserId = "201001505";
    final static String m_InvestorId = "201001505";
    final static String m_PassWord = "qh120216";
    final static String m_TradingDay = "20210617";
    final static String m_AccountId = "201001505";
    final static String m_CurrencyId = "CNY";

    /**
     * 查询持仓、每一手单价、对锁状态、账户余额
     *
     * @param ctpFuture
     */
    public void query(CTPFuture ctpFuture) {
        yesterdayBuyPosition = ctpFuture.getYesterdayBuyPosition();
        todayBuyPosition = ctpFuture.getTodayBuyPosition();
        yesterdaySellPosition = ctpFuture.getYesterdaySellPosition();
        todaySellPosition = ctpFuture.getTodaySellPosition();
        unitPrice = ctpFuture.getUnitPrice();
        pairLock = ctpFuture.getPairLock();
        availableMoney = accountMap.get("Available");
    }

    /**
     * 买开对锁
     *
     * @param
     * @param price
     * @param volume
     * @param strategyName
     * @return
     */
    public int buyOpenPairLock(String code, double price, int volume, String strategyName) {
        CTPFuture ctpFuture = focusContainer.get(code);
        query(ctpFuture);
        System.out.println("1:" + availableMoney + "  2:" + unitPrice);
        if (pairLock == -1) {
            if (availableMoney >= volume * unitPrice) {
                System.out.println("下单1111111");
                return buyOpen(ctpFuture,price,volume,strategyName);
            } else {
                System.out.println("可用资金不足！11111");
                return -1; //alert lack of money
            }
        }
        if (yesterdaySellPosition < volume) {
            if (availableMoney >= volume * unitPrice) {
                System.out.println("下单2222222");
                return buyOpen(ctpFuture,price,volume,strategyName);
            } else {
                if (todaySellPosition > 0) {
                    System.out.println("下单3333333");
                    return buyClose(ctpFuture, price, volume,strategyName);
                } else {
                    System.out.println("可用资金不足！22222");
                    return -1; //alert lack of money
                }
            }
        } else {
            if (todayBuyPosition > 0) {
                if (availableMoney >= volume * unitPrice) {
                    System.out.println("下单444444");
                    return buyOpen(ctpFuture, price, volume,strategyName);
                } else {
                    System.out.println("下单5555555");
                    return buyCloseYesterday(ctpFuture, price, volume,strategyName);
                }
            } else {
                System.out.println("下单6666");
                return buyCloseYesterday(ctpFuture, price, volume, strategyName);
            }
        }
    }

    /**
     * 买平对锁
     *
     * @param
     * @param price
     * @param volume
     * @param strategyName
     * @return
     */
    public int buyClosePairLock(String code, double price, int volume, String strategyName) {
        CTPFuture ctpFuture = focusContainer.get(code);
        if (pairLock == -1) {
            return buyClose(ctpFuture, price, volume, strategyName);
        }
        query(ctpFuture);
        if (yesterdaySellPosition < volume) {
            if (availableMoney >= volume * unitPrice) {
                return buyOpen(ctpFuture, price, volume, strategyName);
            } else {
                if (todaySellPosition > 0) {
                    return buyCloseToday(ctpFuture, price, volume, strategyName);
                } else {
                    System.out.println("可用资金不足！");
                    return -1; //alert lack of money
                }
            }
        } else {
            if (todayBuyPosition > 0) {
                if (availableMoney >= volume * unitPrice) {
                    return buyOpen(ctpFuture, price, volume, strategyName);
                } else {
                    return buyCloseYesterday(ctpFuture, price, volume, strategyName);
                }
            } else {
                return buyCloseYesterday(ctpFuture, price, volume, strategyName);
            }
        }
    }

    /**
     * 卖开对锁
     *
     * @param
     * @param price
     * @param volume
     * @param strategyName
     * @return
     */
    public int sellOpenPairLock(String code, double price, int volume, String strategyName) {
        CTPFuture ctpFuture = focusContainer.get(code);
        if (pairLock == -1) {
            if (availableMoney >= volume * unitPrice) {
                return sellOpen(ctpFuture, price, volume, strategyName);
            } else {
                System.out.println("可用资金不足！");
                return -1; //alert lack of money
            }
        }
        query(ctpFuture);
        if (yesterdayBuyPosition < volume) {
            if (availableMoney >= volume * unitPrice) {
                return sellOpen(ctpFuture, price, volume, strategyName);
            } else {
                if (todayBuyPosition > 0) {
                    return sellCloseToday(ctpFuture, price, volume, strategyName);
                } else {
                    System.out.println("可用资金不足！");
                    return -1; //alert lack of money
                }
            }
        } else {
            if (todaySellPosition > 0) {
                if (availableMoney >= volume * unitPrice) {
                    return sellOpen(ctpFuture, price, volume, strategyName);
                } else {
                    return sellCloseYesterday(ctpFuture, price, volume, strategyName);
                }
            } else {
                return sellCloseYesterday(ctpFuture, price, volume, strategyName);
            }
        }
    }

    /**
     * 卖平对锁
     *
     * @param
     * @param price
     * @param volume
     * @param strategyName
     * @return
     */
    public int sellClosePairLock(String code, double price, int volume, String strategyName) {
        CTPFuture ctpFuture = focusContainer.get(code);
        if (pairLock == -1) {
            return sellClose(ctpFuture, price, volume, strategyName);
        }
        query(ctpFuture);
        if (yesterdayBuyPosition < volume) {
            if (availableMoney < unitPrice * volume) {
                if (todayBuyPosition > 0) {
                    return sellCloseToday(ctpFuture, price, volume, strategyName);
                } else {
                    System.out.println("可用资金不足！");
                    return -1; //alert lack of money
                }
            } else {
                return sellOpen(ctpFuture, price, volume, strategyName);
            }
        } else {
            if (todaySellPosition > 0) {
                if (availableMoney > unitPrice * volume) {
                    return sellOpen(ctpFuture, price, volume, strategyName);
                } else {
                    return sellCloseYesterday(ctpFuture, price, volume, strategyName);
                }
            } else {
                return sellCloseYesterday(ctpFuture, price, volume, strategyName);
            }
        }
    }

    /**
     * 注意： OrderRef传给CTP是"编号,策略名称,理论价格"的格式， CTP将OrderRef返回后需要分割处理以存入数据库
     *
     * @param ctpFuture
     * @param price
     * @param volume
     * @param startegyName
     * @return
     */
    public int buyOpen(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 买开" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Buy);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_Open));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price + 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        // 本地持仓变化指令
        int todayBuyPosi = ctpFuture.getTodayBuyPosition() + volume;
        ctpFuture.setTodayBuyPosition(todayBuyPosi);
        return status;
    }

    public int buyClose(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 买平" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Buy);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_Close));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price - 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int todayBuyPosi = ctpFuture.getTodayBuyPosition() - volume;
        ctpFuture.setTodayBuyPosition(todayBuyPosi);
        return status;
    }

    public int sellOpen(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 卖开" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Sell);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_Open));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price - 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int todaySellPosi = ctpFuture.getTodaySellPosition() + volume;
        ctpFuture.setTodayBuyPosition(todaySellPosi);
        return status;
    }

    public int sellClose(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 卖开" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Sell);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_Close));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price + 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int todaySellPosi = ctpFuture.getTodaySellPosition() - volume;
        ctpFuture.setTodayBuyPosition(todaySellPosi);
        return status;
    }

    public int buyCloseToday(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 买平今" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Buy);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_CloseToday));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price - 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int todayBuyPosi = ctpFuture.getTodaySellPosition() - volume;
        ctpFuture.setTodayBuyPosition(todayBuyPosi);
        return status;
    }

    public int buyCloseYesterday(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 买平昨" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Buy);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_CloseYesterday));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price - 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int yesterdayBuyPosi = ctpFuture.getTodaySellPosition() - volume;
        ctpFuture.setTodayBuyPosition(yesterdayBuyPosi);
        return status;
    }


    public int sellCloseToday(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 卖平今" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Sell);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_CloseToday));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price + 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
//        int status = 1;
        int status=traderApi.ReqOrderInsert(orderField,req);
        int todaySellPosi = ctpFuture.getTodaySellPosition() - volume;
        ctpFuture.setTodayBuyPosition(todaySellPosi);
        return status;
    }

    public int sellCloseYesterday(CTPFuture ctpFuture, double price, int volume, String startegyName) {
        System.out.println(ctpFuture.getCode() + "期货, " + price + "元/手, 卖平昨" + volume + "手");
        CThostFtdcInputOrderField orderField = new CThostFtdcInputOrderField();
        orderField.setBrokerID(m_BrokerId);
        orderField.setInvestorID(m_InvestorId);
        orderField.setExchangeID(ctpFuture.getExchange());
        orderField.setInstrumentID(ctpFuture.getCode());
        orderField.setUserID("00001");
        orderField.setOrderPriceType(thosttraderapiConstants.THOST_FTDC_OPT_LimitPrice);
        orderField.setDirection(thosttraderapiConstants.THOST_FTDC_D_Sell);
        orderField.setCombOffsetFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_OF_CloseYesterday));
        orderField.setCombHedgeFlag(String.valueOf(thosttraderapiConstants.THOST_FTDC_HF_Speculation));
        orderField.setLimitPrice(price + 10);
        orderField.setVolumeTotalOriginal(volume);
        orderField.setTimeCondition(thosttraderapiConstants.THOST_FTDC_TC_IOC);
        orderField.setVolumeCondition(thosttraderapiConstants.THOST_FTDC_VC_CV);
        orderField.setMinVolume(1);
        orderField.setContingentCondition(thosttraderapiConstants.THOST_FTDC_CC_Immediately);
        orderField.setStopPrice(0);
        orderField.setForceCloseReason(thosttraderapiConstants.THOST_FTDC_FCC_NotForceClose);
        orderField.setIsAutoSuspend(0);
        int req = refMap.get("requestID") + 1;
        orderField.setOrderRef(req + "," + startegyName + "," + price);
        refMap.replace("requestID", req);
        redisUtil.set("requestID", req);
        int status=traderApi.ReqOrderInsert(orderField,req);
//        int status = 1;
        int yesterdaySellPosi = ctpFuture.getTodaySellPosition() - volume;
        ctpFuture.setTodayBuyPosition(yesterdaySellPosi);
        return status;
    }

    /**
     * 开盘前一分钟无效订单重新下单
     */
    public void completeEarlyOrder() {
        for (OrderEntity orderEntity : incompleteOrder) {
            double price = orderEntity.getIdealPrice();
            int volume = orderEntity.getVolume();
            String strategyName = orderEntity.getStrategy();
            if (orderEntity.getDirection().equals("0")) { // Buy
                if (orderEntity.getOffsetFlag().equals("0")) { // Open
                    buyOpenPairLock(orderEntity.getInstrument(), price, volume, strategyName);
                } else { // Close
                    buyClosePairLock(orderEntity.getInstrument(), price, volume, strategyName);
                }
            } else { //Sell
                if (orderEntity.getOffsetFlag().equals("0")) { // Open
                    sellOpenPairLock(orderEntity.getInstrument(), price, volume, strategyName);
                } else { // Close
                    sellClosePairLock(orderEntity.getInstrument(), price, volume, strategyName);
                }
            }
        }
    }


}
