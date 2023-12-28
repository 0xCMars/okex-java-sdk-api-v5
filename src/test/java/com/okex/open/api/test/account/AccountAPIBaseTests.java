package com.okex.open.api.test.account;

import com.okex.open.api.config.APIConfiguration;
import com.okex.open.api.enums.I18nEnum;
import com.okex.open.api.test.BaseTests;

/**
 * Account api basetests
 *
 * @author hucj
 * @version 1.0.0
 * @date 2018/7/04 18:23
 */
public class AccountAPIBaseTests extends BaseTests {

    public APIConfiguration config() {
        APIConfiguration config = new APIConfiguration();

        config.setEndpoint("https://www.okex.com/");
        // apiKey，api注册成功后页面上有


        config.setApiKey("62bc24ef-7936-493e-bcab-2baa6d6564d7");
        config.setSecretKey("C3CB4976543584964EF5A4855FEB0F25");
        config.setPassphrase("Account98..");


        config.setPrint(true);
        config.setI18n(I18nEnum.ENGLISH);

        return config;
    }


}
