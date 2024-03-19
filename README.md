# okex-java-sdk-api-v5

## Usage

参考pubDemo的方式获得市场价格ticker，trades，orderbook等，在使用时可以重新实现WebSocketListener接口，从而获得ws发送来的信息

如果需要login可以参考demoAccount，但需要注意修改URL到wss://ws.okx.com:8443/ws/v5/private