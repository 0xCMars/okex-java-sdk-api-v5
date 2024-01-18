package com.okex.open.api.wsService.publicUrl;

public interface pubWsService {

    // Market Data
    // This channel uses public WebSocket and authentication is not required.
    String getTickers(String instId);

    // Retrieve the recent trades data. Data will be pushed whenever there is a trade.
    String getTrades(String instId);

    // Retrieve order book data.
    //Use books for 400 depth levels,
    //    books5 for 5 depth levels,
    //    bbo-tbt tick-by-tick 1 depth level,
    //    books50-l2-tbt tick-by-tick 50 depth levels,
    //    and books-l2-tbt for tick-by-tick 400 depth levels.
    String getOrdBooks(String channel, String instId);


    // Public Data
    // There is more operation in this Section
    String getInstrument(String instType);

    // Retrieve the open interest
    String getOpenInterest(String instId);

    // Retrieve the mark price.
    String getMarkPrice(String instId);


}
