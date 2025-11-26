package com.sparta.payment_system.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 결제화면 form 컨트롤러
 */
@Controller
@RequestMapping("/api/payment")
public class PaymentViewController {

    @Value("${portone.store.id}")
    private String storeId;

    @Value("${portone.channel.key}")
    private String channelKey;

    @GetMapping
    public String paymentForm(Model model) {

        model.addAttribute("storeId", storeId); //스토어 아이디
        model.addAttribute("channelKey", channelKey); //채널 키
        model.addAttribute("userId", 1); // 결제 하는 userId

        //model.addAttribute("order_", new Payment());

        String paymentId = "payment-" + System.currentTimeMillis();
        model.addAttribute("paymentId", paymentId);
        return "paymentForm";
    }
}