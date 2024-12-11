package com.okex.open.api.test.trade;

import com.okex.open.api.config.APIConfiguration;
import com.okex.open.api.enums.I18nEnum;
import com.okex.open.api.test.BaseTests;

public class TradeAPIBaseTest extends BaseTests {
    public APIConfiguration config() {
        APIConfiguration config = new APIConfiguration();

        config.setEndpoint("https://www.okx.com/");


        config.setApiKey("7349788e-3776-4649-9151-4cc8d16afd9b");
        config.setSecretKey("DFB0CC0AF519D142DA4B1671515BAE13");
        config.setPassphrase("Qweasdzxc11!");


        config.setPrint(true);
       /* config.setI18n(I18nEnum.SIMPLIFIED_CHINESE);*/
        config.setI18n(I18nEnum.ENGLISH);
        return config;
    }


}
