package com.okex.open.api.wsService.accout;

public interface AccountWSService {

    // Get the balance of all assets in the account
    String getBalance();

    // Get the balance of one asset in the account
    String getBalance(String ccy);

    // Get Positions channel
    String getPositions(String instType, String instFamily, String instId);

    // Retrieve account balance and position information.
    String getBalanceAndPosition();

    // only used as a risk warning, and is not recommended as a risk judgment for strategic trading
    String getLiquidationWarning(String instType);

    // Retrieve account greeks information.
    String getAccountGreeks();

    String getAccountGreeks(String ccy);

}
