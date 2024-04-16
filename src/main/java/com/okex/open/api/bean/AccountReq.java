package com.okex.open.api.bean;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountReq extends SubscribeReq{
    private String ccy;

    public AccountReq(String ccy, String channel) {
        super("", channel);
        this.ccy = ccy;
    }

}
