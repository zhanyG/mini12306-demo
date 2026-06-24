package com.example.demo.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付宝支付配置：从 application.properties 读取沙箱环境参数，
 * 并创建 AlipayClient Bean。
 *
 * 使用前需要在 https://open.alipay.com 的沙箱环境中完成以下步骤：
 * 1. 进入「沙箱环境」→「沙箱应用」，获取 APP_ID
 * 2. 使用「支付宝开放平台开发助手」生成 RSA2 密钥
 * 3. 将应用公钥上传到沙箱应用配置中
 * 4. 将生成的商户私钥（PKCS8格式）填入 alipay.app-private-key
 * 5. 将支付宝公钥填入 alipay.alipay-public-key
 */
@Configuration
public class AlipayConfig {

    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.gateway-url}")
    private String gatewayUrl;

    @Value("${alipay.app-private-key}")
    private String appPrivateKey;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,       // 沙箱网关
                appId,            // 应用 APP_ID
                appPrivateKey,    // 商户私钥
                "json",           // 格式
                "UTF-8",          // 字符集
                alipayPublicKey,  // 支付宝公钥
                "RSA2"            // 签名算法
        );
    }
}
