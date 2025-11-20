package com.sparta.payment_system.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class PortOneClient {

	private final WebClient webClient;
	private final String apiSecret;

	public PortOneClient(@Value("${portone.api.url}") String apiUrl,
		@Value("${portone.api.secret}") String apiSecret) {
		this.webClient = WebClient.create(apiUrl); // WebClient로 인해서 Mono 는 필수
		this.apiSecret = apiSecret;
	}

	// API Secret으로 인증 토큰 요청
	public Mono<String> getAccessToken() {
		return webClient.post()
			.uri("/login/api-secret")
			.bodyValue(Map.of("apiSecret", apiSecret))
			.retrieve()
			.bodyToMono(Map.class)
			.map(response -> (String)response.get("accessToken"));
	}

	// 결제 ID로 결제 정보 조회
	public Mono<Map> getPaymentDetails(String impUid, String accessToken) {
		return webClient.get()
			.uri("/payments/{impUid}", impUid)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(Map.class);
	}

	// 결제 취소
	public Mono<Map> cancelPayment(String impUid, String accessToken, String reason) {
		return webClient.post()
			.uri("/payments/{impUid}/cancel", impUid)
			.header("Authorization", "Bearer " + accessToken)
			.bodyValue(Map.of("reason", reason))
			.retrieve()
			.bodyToMono(Map.class);
	}
}
