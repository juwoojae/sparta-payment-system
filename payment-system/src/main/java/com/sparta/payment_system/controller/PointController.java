package com.sparta.payment_system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.dto.point.GetPointsRequest;
import com.sparta.payment_system.dto.point.GetPointsResponse;
import com.sparta.payment_system.service.PointService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	@GetMapping("/api/points")
	public ResponseEntity<GetPointsResponse> getUserPoints(GetPointsRequest  getPointsRequest) {
		GetPointsResponse response = pointService.getUserTotalPoints(getPointsRequest);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}
}
