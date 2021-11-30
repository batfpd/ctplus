package com.ctplus.common;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public abstract class StrategyObserver {

    protected String name;
    protected String code;
    protected int direction;
    protected int enterNo;
    protected int realTrade;

    protected double price;
    protected String status;


    public abstract void update(double price,String ts);

    public abstract StrategyObserver clone(String newCode);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrategyObserver that = (StrategyObserver) o;
        return name.equals(that.name) && code.equals(that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, code);
    }
}
