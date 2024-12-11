package com.okex.open.api.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderReq extends SubscribeReq{
    private String instType;

    public OrderReq(String instType) {
        super("", "orders");
        this.instType = instType;
    }

}
