package com.chuang.urras.support;

public class BiValue<ONE, TWO> {
    private ONE one;
    private TWO two;

    public BiValue(ONE one, TWO two) {
        this.one = one;
        this.two = two;
    }

    public TWO getTwo() {
        return two;
    }

    public void setTwo(TWO two) {
        this.two = two;
    }

    public ONE getOne() {
        return one;
    }

    public void setOne(ONE one) {
        this.one = one;
    }


}
