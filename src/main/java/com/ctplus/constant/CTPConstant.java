package com.ctplus.constant;


public interface CTPConstant {

    // 报价类型
    // TThostFtdcOrderPriceType  OrderPriceType;
    // 市价
    char THOST_FTDC_OPT_AnyPrice = '1';
    // 限价/条件单
    char THOST_FTDC_OPT_LimitPrice = '2';
    // 最优价
    char THOST_FTDC_OPT_BestPrice = '3';
    // 最新价
    char THOST_FTDC_OPT_LastPrice = '4';
    // 最新价浮动上浮1个ticks
    char THOST_FTDC_OPT_LastPricePlusOneTicks = '5';
    // 最新价浮动上浮2个ticks
    char THOST_FTDC_OPT_LastPricePlusTwoTicks = '6';
    // 最新价浮动上浮3个ticks
    char THOST_FTDC_OPT_LastPricePlusThreeTicks = '7';
    // 卖一价
    char THOST_FTDC_OPT_AskPrice1 = '8';
    // 卖一价浮动上浮1个ticks
    char THOST_FTDC_OPT_AskPrice1PlusOneTicks = '9';
    // 卖一价浮动上浮2个ticks
    char THOST_FTDC_OPT_AskPricePlusTwoTicks = 'A';
    // 卖一价浮动上浮3个ticks
    char THOST_FTDC_OPT_AskPrice1PlusThreeTicks = 'B';
    // 买一价
    char THOST_FTDC_OPT_BidPrice1 = 'C';
    // 买一价浮动上浮1个ticks
    char THOST_FTDC_OPT_BidPrice1PlusOneTicks = 'D';
    // 买一价浮动上浮2个ticks
    char THOST_FTDC_OPT_BidPrice1PlusTwoTicks = 'E';
    // 买一价浮动上浮3个ticks
    char THOST_FTDC_OPT_BidPrice1PlusThreeTicks = 'F';

    // 交易方向
    // TThostFtdcDirectionType  Direction;
    // 买
    char THOST_FTDC_D_Buy = '0';
    // 卖
    char THOST_FTDC_D_Sell = '1';

    // 组合开平标志
    // TThostFtdcCombOffsetFlagType  CombOffsetFlag;
    // 开仓
    char THOST_FTDC_OF_Open = '0';
    // 平仓
    char THOST_FTDC_OF_Close = '1';
    // 强平
    char THOST_FTDC_OF_ForceClose = '2';
    // 平今
    char THOST_FTDC_OF_CloseToday = '3';
    // 平昨
    char THOST_FTDC_OF_CloseYesterday = '4';
    // 强减
    char THOST_FTDC_OF_ForceOff = '5';
    // 本地强平
    char THOST_FTDC_OF_LocalForceClose = '6';

    // 组合投机套保标志
    // TThostFtdcCombHedgeFlagType  CombHedgeFlag;
    // 投机
    char THOST_FTDC_HF_Speculation = '1';
    // 套利
    char THOST_FTDC_HF_Arbitrage = '2';
    // 套保
    char THOST_FTDC_HF_Hedge = '3';


    // 有效期类型
    // TThostFtdcTimeConditionType TimeCondition;
    // 立即完成，否则撤销
    char THOST_FTDC_TC_IOC = '1'; //市价
    // 本节有效
    char THOST_FTDC_TC_GFS = '2';
    // 当日有效
    char THOST_FTDC_TC_GFD = '3';//限价、条件单
    // 指定日期前有效
    char THOST_FTDC_TC_GTD = '4';
    // 撤销前有效
    char THOST_FTDC_TC_GTC = '5';
    // 集合竞价有效
    char THOST_FTDC_TC_GFA = '6';


    // 成交量类型
    // TThostFtdcVolumeConditionType VolumeCondition;
    // 任何数量
    char THOST_FTDC_VC_AV = '1'; //普遍用这个
    // 最小数量
    char THOST_FTDC_VC_MV = '2';
    // 全部数量
    char THOST_FTDC_VC_CV = '3';

    //触发条件
    // TThostFtdcContingentConditionType ContingentCondition;
    //立即
    char THOST_FTDC_CC_Immediately = '1';
    // 止损
    char THOST_FTDC_CC_Touch = '2';
    // 止赢
    char THOST_FTDC_CC_TouchProfit = '3';
    // 预埋单
    char THOST_FTDC_CC_ParkedOrder = '4';
    // 最新价大于条件价
    char THOST_FTDC_CC_LastPriceGreaterThanStopPrice = '5';
    // 最新价大于等于条件价
    char THOST_FTDC_CC_LastPriceGreaterEqualStopPrice = '6';
    // 最新价小于条件价
    char THOST_FTDC_CC_LastPriceLesserThanStopPrice = '7';
    // 最新价小于等于条件价
    char THOST_FTDC_CC_LastPriceLesserEqualStopPrice = '8';
    // 卖一价大于条件价
    char THOST_FTDC_CC_AskPriceGreaterThanStopPrice = '9';
    // 卖一价大于等于条件价
    char THOST_FTDC_CC_AskPriceGreaterEqualStopPrice = 'A';
    // 卖一价小于条件价
    char THOST_FTDC_CC_AskPriceLesserThanStopPrice = 'B';
    // 卖一价小于等于条件价
    char THOST_FTDC_CC_AskPriceLesserEqualStopPrice = 'C';
    // 买一价大于条件价
    char THOST_FTDC_CC_BidPriceGreaterThanStopPrice = 'D';
    // 买一价大于等于条件价
    char THOST_FTDC_CC_BidPriceGreaterEqualStopPrice = 'E';
    // 买一价小于条件价
    char THOST_FTDC_CC_BidPriceLesserThanStopPrice = 'F';
    // 买一价小于等于条件价
    char THOST_FTDC_CC_BidPriceLesserEqualStopPrice = 'H';


    // 强平原因
    // TThostFtdcForceCloseReasonType ForceCloseReason;

    // 非强平
    char THOST_FTDC_FCC_NotForceClose = '0'; //正常交易选这个
    // 资金不足
    char THOST_FTDC_FCC_LackDeposit = '1';
    // 客户超仓
    char THOST_FTDC_FCC_ClientOverPositionLimit = '2';
    // 会员超仓
    char THOST_FTDC_FCC_MemberOverPositionLimit = '3';
    // 持仓非整数倍
    char THOST_FTDC_FCC_NotMultiple = '4';
    // 违规
    char THOST_FTDC_FCC_Violation = '5';
    // 其它
    char THOST_FTDC_FCC_Other = '6';
    // 自然人临近交割
    char THOST_FTDC_FCC_PersonDeliv = '7';




}
