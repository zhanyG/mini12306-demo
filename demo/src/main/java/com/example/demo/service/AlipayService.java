package com.example.demo.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 支付宝支付服务，封装沙箱环境的支付创建、查询和回调验签。
 */
@Service
public class AlipayService {

    private final AlipayClient alipayClient;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    public AlipayService(AlipayClient alipayClient) {
        this.alipayClient = alipayClient;
    }

    /**
     * 创建支付宝电脑网站支付（返回一个表单页面，前端渲染后自动跳转支付宝收银台）。
     *
     * @param orderNo  订单号
     * @param amount   支付金额
     * @param subject  商品名称
     * @return 自动提交的 HTML 表单
     */
    public String createPayPage(String orderNo, String amount, String subject) {
        return createPayPage(orderNo, amount, subject, returnUrl);
    }

    /**
     * 创建支付宝支付，支持自定义同步跳转地址（用于改签补差价等场景）。
     */
    public String createPayPage(String orderNo, String amount, String subject, String customReturnUrl) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(customReturnUrl);

        // 业务参数
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + orderNo + "\"," +
                "    \"product_code\":\"FAST_INSTANT_TRADE_PAY\"," +
                "    \"total_amount\":" + amount + "," +
                "    \"subject\":\"" + subject + "\"" +
                "}");

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
            if (response.isSuccess()) {
                return response.getBody();  // 返回 HTML 表单
            }
            throw new RuntimeException("支付宝创建支付失败：" + response.getSubMsg());
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝调用异常", e);
        }
    }

    /**
     * 查询交易状态。
     *
     * @param tradeNo 商户订单号
     * @return 交易状态：WAIT_BUYER_PAY / TRADE_SUCCESS / TRADE_FINISHED / TRADE_CLOSED
     */
    public String queryTrade(String tradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + tradeNo + "\"}");

        try {
            AlipayTradeQueryResponse response = alipayClient.execute(request);
            if (response.isSuccess()) {
                return response.getTradeStatus();
            }
            return null;
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝查询异常", e);
        }
    }

    /**
     * 验证支付宝异步通知的签名。
     *
     * @param params 支付宝 POST 回调的所有参数
     * @return 签名验证是否通过
     */
    public boolean verifyNotify(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", AlipayConstants.SIGN_TYPE_RSA2);
        } catch (AlipayApiException e) {
            return false;
        }
    }
}
