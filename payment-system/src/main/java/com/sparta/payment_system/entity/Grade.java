package com.sparta.payment_system.entity;

/**
 * 멤버십 등급제
 */
public enum Grade {
	NOMAL(1), VIP(5), VVIP(10);

	private final Long rate;

	Grade(long rate) {
		this.rate = rate;
	}

	public long getRate() {
		return this.rate;
	}
}
