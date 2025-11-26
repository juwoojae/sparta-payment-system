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
	private final String storeId;

	public PortOneClient(@Value("${portone.api.url}") String apiUrl,
		@Value("${portone.api.secret}") String apiSecret,
		@Value("store-26aa9b80-8368-41c6-b05a-31b92973ec4e") String storeId) {
		this.webClient = WebClient.create(apiUrl); // WebClient로 인해서 Mono 는 필수
		this.apiSecret = apiSecret;
		this.storeId = storeId;
	}

	// API Secret으로 인증 토큰 요청
	public Mono<String> getAccessToken() {
		return webClient.post()
			.uri("/login/api-secret")
			.bodyValue(Map.of("apiSecret", apiSecret))
			.retrieve()
			.bodyToMono(Map.class)
			.map(response -> (String)response.get("accessToken"))
			.onErrorResume(e -> {
				System.err.println("PortOne token fetch error: " + e.getMessage());
				return Mono.empty();
			});
	}

	// 결제 ID로 결제 정보 조회
	public Mono<Map> getPaymentDetails(String impUid, String accessToken) {

		System.out.println("PortOneClient.getPaymentDetails 호츨");
		System.out.println("impUid = " + impUid);
		System.out.println("accessToken = " + accessToken);
		return webClient.get()
			.uri("/payments/{impUid}?storeId={storeId}", impUid, storeId)
			.header("Authorization", "Bearer " + accessToken)
			.retrieve()
			.bodyToMono(Map.class)
			.doOnNext(pd -> System.out.println("결제 조회 응답: " + pd))
			.doOnError(e -> {
				System.err.println("getPaymentDetails 에러: " + e.getMessage());
				e.printStackTrace();
			});
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
