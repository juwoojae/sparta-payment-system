package com.sparta.payment_system.entity;

import java.math.BigDecimal;

/**
 * 멤버십 등급제
 */
public enum Grade {
	NORMAL(1), VIP(5), VVIP(10);

	private final Long rate;

	Grade(long rate) {
		this.rate = rate;
	}

	public long getRate() {
		return this.rate;
	}

	// 등급 등락 결정 기준 설정
	public static Grade decideGrade(BigDecimal totalAmount) {

		if (totalAmount == null) {
			return NORMAL;
		}

		if (totalAmount.compareTo(BigDecimal.valueOf(100_000)) >= 0) {
			return VVIP;
		}
		if (totalAmount.compareTo(BigDecimal.valueOf(50_000)) >= 0) {
			return VIP;
		}
		return NORMAL;
	}

}
