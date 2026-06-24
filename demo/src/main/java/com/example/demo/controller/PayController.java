package com.example.demo.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.example.demo.entity.Order;
import com.example.demo.entity.Train;
import com.example.demo.repository.TrainRepository;
import com.example.demo.service.AlipayService;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付相关 API：支付宝支付、异步通知、查询。
 */
@RestController
@RequestMapping("/api/pay")
public class PayController {

    private final AlipayService alipayService;
    private final OrderService orderService;
    private final TrainRepository trainRepository;

    @Value("${alipay.alipay-public-key}")
    private String alipayPublicKey;

    @Value("${alipay.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public PayController(AlipayService alipayService, OrderService orderService, TrainRepository trainRepository) {
        this.alipayService = alipayService;
        this.orderService = orderService;
        this.trainRepository = trainRepository;
    }

    /**
     * 获取支付宝支付页面（返回 HTML，浏览器渲染后跳转支付宝收银台）。
     */
    @GetMapping("/alipay/page/{orderId}")
    public void alipayPage(@PathVariable Long orderId, HttpServletResponse response) throws IOException {
        Order order = orderService.getOrderById(orderId);
        if (!"未支付".equals(order.getStatus())) {
            response.getWriter().write("订单已支付或已过期");
            return;
        }

        String html = alipayService.createPayPage(
                order.getOrderNo(),
                String.format("%.2f", order.getPrice()),
                "火车票 " + order.getOrderNo()
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 支付宝改签补差价支付页面 — 按差价金额生成支付宝收银台页面。
     * 支付完成后跳转到前端订单页，用户可以自行确认改签。
     */
    @GetMapping("/alipay/page-upgrade/{orderId}")
    public void alipayPageUpgrade(@PathVariable Long orderId, @RequestParam Long newTrainId, HttpServletResponse response) throws IOException {
        Order order = orderService.getOrderById(orderId);
        Train newTrain = trainRepository.findById(newTrainId)
                .orElseThrow(() -> new RuntimeException("新车次不存在"));
        double diff = newTrain.getPrice() - order.getPrice();
        if (diff <= 0) {
            response.getWriter().write("无需补差价");
            return;
        }

        String html = alipayService.createPayPage(
                order.getOrderNo() + "_UPGRADE",
                String.format("%.2f", diff),
                "改签补差价 " + order.getOrderNo(),
                "http://localhost:5175/orders"
        );

        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 本地模拟支付页面 - 显示原价和实付 0.01 元，含确认支付按钮。
     */
    @GetMapping("/page/{orderId}")
    public void mockPayPage(@PathVariable Long orderId, HttpServletResponse response) throws IOException {
        Order order = orderService.getOrderById(orderId);
        response.setContentType("text/html;charset=UTF-8");
        String html = buildMockPayHtml(order);
        response.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 本地模拟支付确认 - 完成支付流程，金额固定 0.01 元。
     */
    @PostMapping("/mock-pay/{orderId}")
    public Map<String, Object> mockPay(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        boolean success = orderService.simulatePay(orderId) != null;
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "支付成功" : "支付失败");
        return result;
    }

    /** 构建本地模拟支付 HTML 页面 */
    private String buildMockPayHtml(Order order) {
        return "<!DOCTYPE html>\n" +
        "<html lang=\"zh-CN\">\n" +
        "<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "<title>支付宝支付</title>\n" +
        "<style>\n" +
        "  * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
        "  body { font-family: -apple-system, 'Helvetica Neue', sans-serif; background: #f5f5f5; display: flex; justify-content: center; align-items: center; min-height: 100vh; }\n" +
        "  .container { background: #fff; width: 380px; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 20px rgba(0,0,0,0.1); }\n" +
        "  .header { background: linear-gradient(135deg, #1677ff, #0958d9); color: #fff; padding: 24px; text-align: center; }\n" +
        "  .header h2 { font-size: 18px; margin-bottom: 4px; }\n" +
        "  .header .sub { font-size: 13px; opacity: 0.85; }\n" +
        "  .body { padding: 20px 24px; }\n" +
        "  .info-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; color: #333; border-bottom: 1px solid #f0f0f0; }\n" +
        "  .info-row .label { color: #999; }\n" +
        "  .price-section { text-align: center; padding: 16px 0; }\n" +
        "  .price-section .pay-price { font-size: 36px; color: #1677ff; font-weight: 700; margin: 8px 0; }\n" +
        "  .price-section .pay-price span { font-size: 16px; }\n" +
        "  .qrcode-box { text-align: center; padding: 16px 0; }\n" +
        "  .qrcode-box canvas, .qrcode-box img { width: 180px; height: 180px; display: block; margin: 0 auto; border: 1px solid #eee; padding: 8px; border-radius: 8px; }\n" +
        "  .qrcode-box .hint { font-size: 13px; color: #999; margin-top: 8px; }\n" +
        "  .btn { display: block; width: 100%; padding: 14px; border: none; border-radius: 8px; font-size: 16px; cursor: pointer; margin-top: 16px; }\n" +
        "  .btn-primary { background: #1677ff; color: #fff; }\n" +
        "  .btn-primary:hover { background: #0958d9; }\n" +
        "  .btn-primary:disabled { background: #a0c4ff; cursor: not-allowed; }\n" +
        "  .btn-success { background: #52c41a; color: #fff; }\n" +
        "  .status-bar { text-align: center; padding: 12px; font-size: 14px; display: none; }\n" +
        "  .status-bar.success { display: block; color: #52c41a; }\n" +
        "  .status-bar.fail { display: block; color: #ff4d4f; }\n" +
        "  .footer { text-align: center; padding: 12px; font-size: 12px; color: #ccc; }\n" +
        "</style>\n" +
        "</head>\n" +
        "<body>\n" +
        "<div class=\"container\">\n" +
        "  <div class=\"header\">\n" +
        "    <h2>支付宝支付</h2>\n" +
        "    <div class=\"sub\">支付宝</div>\n" +
        "  </div>\n" +
        "  <div class=\"body\">\n" +
        "    <div class=\"info-row\"><span class=\"label\">订单号</span><span>" + order.getOrderNo() + "</span></div>\n" +
        "    <div class=\"info-row\"><span class=\"label\">商品</span><span>火车票</span></div>\n" +
        "    <div class=\"price-section\">\n" +
        "      <div class=\"pay-price\"><span>¥</span>" + String.format("%.2f", order.getPrice()) + "</div>\n" +
        "    </div>\n" +
        "    <div class=\"qrcode-box\">\n" +
        "      <div id=\"qrcode\"></div>\n" +
        "      <div class=\"hint\">请使用支付宝扫描二维码付款</div>\n" +
        "    </div>\n" +
        "    <button class=\"btn btn-primary\" id=\"payBtn\" onclick=\"confirmPay()\">我已付款</button>\n" +
        "    <div class=\"status-bar\" id=\"statusBar\"></div>\n" +
        "  </div>\n" +
        "  <div class=\"footer\">支付金额 ¥" + String.format("%.2f", order.getPrice()) + "</div>\n" +
        "</div>\n" +
        "<script src=\"https://cdn.jsdelivr.net/npm/qrcodejs@1.0.0/qrcode.min.js\"></script>\n" +
        "<script>\n" +
        "  new QRCode(document.getElementById('qrcode'), { text: 'alipays://platformapi/startApp?appId=20000067&orderId=" + order.getId() + "', width: 180, height: 180 });\n" +
        "  function confirmPay() {\n" +
        "    var btn = document.getElementById('payBtn');\n" +
        "    btn.disabled = true; btn.textContent = '处理中...'; btn.className = 'btn btn-primary';\n" +
        "    fetch('/api/pay/mock-pay/" + order.getId() + "', { method: 'POST' })\n" +
        "      .then(function(r) { return r.json(); })\n" +
        "      .then(function(d) {\n" +
        "        if (d.success) {\n" +
        "          document.getElementById('statusBar').textContent = '\\u2714 ' + d.message;\n" +
        "          document.getElementById('statusBar').className = 'status-bar success';\n" +
        "          btn.textContent = '支付成功';\n" +
        "          btn.className = 'btn btn-success';\n" +
        "          setTimeout(function() { window.location.href = '/orders'; }, 2000);\n" +
        "        } else {\n" +
        "          document.getElementById('statusBar').textContent = '\\u2716 ' + d.message;\n" +
        "          document.getElementById('statusBar').className = 'status-bar fail';\n" +
        "          btn.disabled = false; btn.textContent = '重新支付'; btn.className = 'btn btn-primary';\n" +
        "        }\n" +
        "      })\n" +
        "      .catch(function() {\n" +
        "        document.getElementById('statusBar').textContent = '\\u2716 \\u7f51\\u7edc\\u5f02\\u5e38';\n" +
        "        document.getElementById('statusBar').className = 'status-bar fail';\n" +
        "        btn.disabled = false; btn.textContent = '重新支付'; btn.className = 'btn btn-primary';\n" +
        "      });\n" +
        "  }\n" +
        "</script>\n" +
        "</body>\n" +
        "</html>";
    }

    /**
     * 支付宝异步通知（POST），支付成功后支付宝会调用此接口。
     * 需要公网可访问，开发期可用 ngrok 内网穿透。
     */
    @PostMapping("/alipay/notify")
    public String alipayNotify(HttpServletRequest request) {
        Map<String, String> params = getParamsFromRequest(request);

        // 验证签名
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
        } catch (AlipayApiException e) {
            return "failure";
        }

        if (!signVerified) {
            return "failure";
        }

        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");   // 商户订单号
        String tradeNo = params.get("trade_no");           // 支付宝交易号

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            // 根据订单号找到订单并确认支付
            // 注意：由于 orderNo 不一定是主键，需要通过 orderNo 查找
            // 简化处理：通过 orderRepository 查找
            orderService.confirmPaymentByOrderNo(outTradeNo, "ALIPAY", tradeNo);
        }

        // 支付宝要求返回 "success"（小写）
        return "success";
    }

    /**
     * 支付宝同步通知（GET 重定向），支付完成后支付宝会将浏览器重定向到此地址。
     * 验证参数后确认支付，然后重定向到前端订单页。
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = getParamsFromRequest(request);

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        if (outTradeNo != null && tradeNo != null) {
            // 验证签名
            boolean signVerified;
            try {
                signVerified = AlipaySignature.rsaCheckV1(params, alipayPublicKey, "UTF-8", "RSA2");
            } catch (AlipayApiException e) {
                signVerified = false;
            }

            if (signVerified) {
                orderService.confirmPaymentByOrderNo(outTradeNo, "ALIPAY", tradeNo);
            }
        }

        response.sendRedirect(frontendUrl + "/orders");
    }

    /**
     * 查询订单支付状态。
     */
    @GetMapping("/alipay/query/{orderId}")
    public Map<String, Object> queryPay(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("status", order.getStatus());

        if ("未支付".equals(order.getStatus())) {
            String tradeStatus = alipayService.queryTrade(order.getOrderNo());
            result.put("tradeStatus", tradeStatus);
        } else {
            result.put("tradeStatus", "TRADE_SUCCESS");
        }
        return result;
    }

    /**
     * 从 HttpServletRequest 提取所有参数。
     */
    private Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            params.put(entry.getKey(), entry.getValue()[0]);
        }
        return params;
    }
}
