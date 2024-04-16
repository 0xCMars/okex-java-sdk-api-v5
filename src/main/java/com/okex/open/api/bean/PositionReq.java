package com.okex.open.api.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionReq extends SubscribeReq{
    private String instType;

    private String instFamily;

    public PositionReq(String instType, String instFamily, String insttId,  String channel) {
        super(insttId, channel);
        this.instType = instType;
        this.instFamily = instFamily;
    }

}
