# 💳 微信支付宝接入指南 (Payment Integration Guide)

目前系统使用的是模拟支付。要接入真实的微信和支付宝支付，您需要进行以下开发步骤。

## 1. 准备工作 (Prerequisites)

在写代码之前，您必须先拥有企业资质并申请以下账号：

*   **微信支付 (WeChat Pay)**: 
    *   注册 [微信支付商户平台](https://pay.weixin.qq.com/)。
    *   获取 `mchId` (商户号), `apiV3Key` (API密钥), `cert.p12` (证书)。
    *   注册 [微信公众平台](https://mp.weixin.qq.com/) 获取 `appId`。
*   **支付宝 (Alipay)**:
    *   注册 [蚂蚁金服开放平台](https://open.alipay.com/)。
    *   创建应用并获取 `appId`, `privateKey` (应用私钥), `alipayPublicKey` (支付宝公钥)。

## 2. 后端改造 (Backend Implementation)

### 2.1 添加依赖 (pom.xml)

在 `ruoyi-common/pom.xml` 或 `ruoyi-admin/pom.xml` 中引入官方/社区优秀的 SDK。

```xml
<!-- 微信支付 (推荐使用 WxJava) -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>wx-java-pay-spring-boot-starter</artifactId>
    <version>4.5.0</version>
</dependency>

<!-- 支付宝 (官方 SDK) -->
<dependency>
    <groupId>com.alipay.sdk</groupId>
    <artifactId>alipay-sdk-java</artifactId>
    <version>4.38.61.ALL</version>
</dependency>
```

### 2.2 配置文件 (application.yml)

在 `ruoyi-admin/src/main/resources/application.yml` 中添加配置：

```yaml
# 微信支付配置
wx:
  pay:
    appId: wx8888888888888888
    mchId: 1230000109
    mchKey: YOUR_API_V2_KEY # 或者 apiV3Key
    keyPath: classpath:cert/apiclient_cert.p12 # 证书路径
    notifyUrl: https://yourdomain.com/agri/payment/wx/notify

# 支付宝配置
alipay:
  appId: 2021000118654876
  privateKey: MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwgg...
  publicKey: MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCg...
  notifyUrl: https://yourdomain.com/agri/payment/alipay/notify
  # 沙箱环境设置为 true
  sandbox: true 
```

### 2.3 编写控制层 (PaymentController)

创建 `AgPaymentController.java`，提供统一下单接口。

```java
@RestController
@RequestMapping("/agri/payment")
public class AgPaymentController {

    @Autowired
    private WxPayService wxPayService; // WxJava 自动注入

    // 1. 获取支付二维码 (Native支付)
    @PostMapping("/{orderId}/native")
    public AjaxResult getPayQrCode(@PathVariable Long orderId, @RequestParam String payType) {
        AgOrder order = orderService.selectAgOrderById(orderId);
        
        if ("WECHAT".equals(payType)) {
            WxPayNativeRequest request = new WxPayNativeRequest();
            request.setBody("农产品订单-" + order.getOrderNo());
            request.setOutTradeNo(order.getOrderNo());
            request.setTotalFee(order.getTotalAmount().multiply(new BigDecimal(100)).intValue()); // 分
            request.setSpbillCreateIp(IpUtils.getIpAddr());
            request.setNotifyUrl("your_notify_url");
            
            // 调用微信接口获取 code_url
            String codeUrl = wxPayService.createOrder(request); 
            return AjaxResult.success("操作成功", codeUrl);
            
        } else if ("ALIPAY".equals(payType)) {
             // 调用支付宝预下单接口 (AlipayTradePrecreateRequest)
             // 返回 qr_code
        }
        return AjaxResult.error("不支持的支付方式");
    }

    // 2. 支付回调 (这是最关键的！)
    @PostMapping("/wx/notify")
    public String wxNotify(@RequestBody String xmlData) {
        // 解析回调，验签
        // 成功后：Update ag_order SET status = '1', pay_time = NOW() WHERE order_no = ...
        return "<xml>...</xml>"; // 返回给微信成功响应
    }
}
```

## 3. 前端改造 (Frontend Implementation)

修改 `ruoyi-ui/src/views/agri/cart/index.vue` 和 `order/index.vue`。

### 3.1 替换模拟图片

**当前代码:**
```html
<img src="https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=MockPayment">
```

**改造后代码:**
```html
<img :src="qrCodeUrl" style="width: 200px; height: 200px;">
```

### 3.2 调用后端接口

```javascript
// 在 handlePay 或 confirmPayment 中
getPayQrCode(orderId, 'WECHAT').then(response => {
    // 后端返回真实的 weixin://wxpay/bizpayurl?pr=...
    // 前端使用 qriously 或 vue-qr 库将链接转为图片
    this.qrCodeUrl = response.msg; // 或者自己在前端生成二维码
    this.openPayment = true;
    
    // 开启轮询检查订单状态
    this.startCheckOrderStatus(orderId);
});

// 轮询方法
startCheckOrderStatus(orderId) {
    this.timer = setInterval(() => {
        checkStatus(orderId).then(res => {
            if (res.data.status === '1') {
                clearInterval(this.timer);
                this.$modal.msgSuccess("支付成功！");
                this.openPayment = false;
            }
        });
    }, 3000); // 每3秒查一次
}
```

## 4. 核心逻辑变化

| 功能 | 现有逻辑 (Web 模拟) | 真实支付逻辑 (Production) |
| :--- | :--- | :--- |
| **二维码** | 静态生成，内容为 "MockPayment" | **动态生成**，内容为微信/支付宝的深度链接 |
| **支付动作** | 用户点击 "我已完成支付" 按钮 | 用户**扫码支付**，手机端确认 |
| **状态变更** | 前端按钮触发后端更新 | **后端回调** (Webhook) 触发更新 |
| **用户体验** | 手动点确认 | 扫码后页面**自动跳转** (轮询/WebSocket) |

## 5. 常见问题

*   **内网穿透**: 微信/支付宝服务器需要回调您的接口。在本地开发时，必须使用 `Cloudflare Tunnel` 或 `Natapp` 将本地 `8080` 暴露到公网，并将公网域名填入 `notifyUrl`。
*   **沙箱环境**: 支付宝提供完善的沙箱环境 (Sandbox)，推荐开发阶段使用沙箱账号测试，无需真实扣款。微信支付目前没有好用的沙箱，通常需要真实支付 0.01 元测试。
