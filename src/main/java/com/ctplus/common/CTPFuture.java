package com.ctplus.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * 期货品种
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CTPFuture extends FocusSubject {

    private String code;
    private double price;
    private double unitPrice;
//    private Date startDate;
//    private Date endDate;
    private String exchange;
    private int yesterdayBuyPosition;
    private int todayBuyPosition;
    private int yesterdaySellPosition;
    private int todaySellPosition;
    private int pairLock; // 1 对锁， 0 不对锁


    public CTPFuture(String code, double unitPrice){
        this.code=code;
        this.unitPrice=unitPrice;
        yesterdayBuyPosition=0;
        todayBuyPosition=0;
        yesterdaySellPosition=0;
        todaySellPosition=0;
    }

    public void change(double newPrice, String ts){
//         if(Math.abs(price-newPrice)>price*0.0005){
        if(price!=newPrice){
            price=newPrice;
            notifyObservers(newPrice, ts);
        }
    }


    @Override
//    @Transactional
    public void notifyObservers(double price, String ts) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("================"+simpleDateFormat.format(Long.parseLong(ts))+"================");
        System.out.println("具体目标"+this.code+"发生改变");
        System.out.println("开始通知策略");
        double start=System.currentTimeMillis();
        int i=0;
        for(Map.Entry<String,StrategyObserver> entry:observerList.entrySet()){
            entry.getValue().update(price, ts);
            i++;
        }
        double end=System.currentTimeMillis();
        double cost=end-start;
        System.out.println(LocalDateTime.now()+"  "+this.code+"共通知"+i+"个策略, 用时"+cost+"毫秒");
        System.out.println("====================================================");
    }

    public boolean addStrategy(StrategyObserver strategyObserver){
        attach(strategyObserver);
        return true;
    }

    public boolean deleteStrategy(String strategyName){
        detach(strategyName);
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CTPFuture ctpFuture = (CTPFuture) o;
        return code.equals(ctpFuture.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, exchange);
    }

    @Override
    public String toString() {
        return "CTPFuture{" +
                "code='" + code + '\'' +
                ", price=" + price +
                ", unitPrice=" + unitPrice +
                ", exchange='" + exchange + '\'' +
                ", yesterdayBuyPosition=" + yesterdayBuyPosition +
                ", todayBuyPosition=" + todayBuyPosition +
                ", yesterdaySellPosition=" + yesterdaySellPosition +
                ", todaySellPosition=" + todaySellPosition +
                ", pairLock=" + pairLock +
                '}';
    }
}
