package com.ctplus.config;

import com.ctplus.common.CTPFuture;
import com.ctplus.entity.strategyEntity.ExampleStrategyEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已关注的期货品种
 */
@Configuration
@EnableAsync
public class ContainerConfig {


    // 存储已经关注行情的Container
    @Bean("focusContainer")
    public HashMap<String, CTPFuture> generateFocusContainer() {
        HashMap<String, CTPFuture> futuresContainer = new HashMap<>();
        return futuresContainer;
    }

    @Bean("strategiesContainer")
    public HashMap<String, HashMap> generateStrategiesContainer() {
        HashMap<String, HashMap> strategies = new HashMap<>();
        HashMap<String, ExampleStrategyEntity> ds2Map = new HashMap<>();
        strategies.put("ExampleStrategyEntity", ds2Map);
        return strategies;
    }

    @Bean("refMap")
    public ConcurrentHashMap<String, Integer> numberRef() {
        return new ConcurrentHashMap<>();
    }

    // 存储期货和交易所
    @Bean("allFutures")
    public ConcurrentHashMap<String, String> allFutures() {
        return new ConcurrentHashMap<>();
    }

    @Bean("accountMap")
    public HashMap<String, Double> pocket() {
        HashMap<String, Double> pocket = new HashMap<>();
        pocket.put("PreMortgage", 0D);
        pocket.put("PreCredit", 0D);
        pocket.put("PreDeposit", 0D);
        pocket.put("PreBalance", 0D);
        pocket.put("PreMargin", 0D);
        pocket.put("InterestBase", 0D);
        pocket.put("Interest", 0D);
        pocket.put("Deposit", 0D);
        pocket.put("Withdraw", 0D);
        pocket.put("FrozenMargin", 0D);
        pocket.put("FrozenCash", 0D);
        pocket.put("FrozenCommission", 0D);
        pocket.put("CurrMargin", 0D);
        pocket.put("CashIn", 0D);
        pocket.put("Commission", 0D);
        pocket.put("CloseProfit", 0D);
        pocket.put("PositionProfit", 0D);
        pocket.put("Balance", 0D);
        pocket.put("Available", 0D);
        pocket.put("WithdrawQuota", 0D);
        pocket.put("Reserve", 0D);
//        pocket.put("SettlementID",0D);
        pocket.put("Credit", 0D);
        pocket.put("Mortgage", 0D);
        pocket.put("ExchangeMargin", 0D);
        pocket.put("DeliveryMargin", 0D);
        pocket.put("ExchangeDeliveryMargin", 0D);
        pocket.put("ReserveBalance", 0D);
        pocket.put("PreFundMortgageIn", 0D);
        pocket.put("PreFundMortgageOut", 0D);
        pocket.put("FundMortgageIn", 0D);
        pocket.put("FundMortgageOut", 0D);
        pocket.put("FundMortgageAvailable", 0D);
        pocket.put("MortgageableFund", 0D);
        pocket.put("SpecProductMargin", 0D);
        pocket.put("SpecProductFrozenMargin", 0D);
        pocket.put("SpecProductCommission", 0D);
        pocket.put("SpecProductFrozenCommission", 0D);
        pocket.put("SpecProductPositionProfit", 0D);
        pocket.put("SpecProductCloseProfit", 0D);
        pocket.put("SpecProductPositionProfitByAlg", 0D);
        pocket.put("SpecProductExchangeMargin", 0D);
        pocket.put("FrozenSwap", 0D);
        pocket.put("RemainSwap", 0D);
        return pocket;
    }


}
