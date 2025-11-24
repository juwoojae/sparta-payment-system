package com.sparta.payment_system.dto.point;

import lombok.Getter;

@Getter
public class GetPointsResponse {

	Integer points;

	public GetPointsResponse(Integer points) {
		this.points = points;
	}
}
