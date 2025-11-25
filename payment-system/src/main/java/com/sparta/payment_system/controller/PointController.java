package com.sparta.payment_system.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.payment_system.dto.point.GetPointsResponse;
import com.sparta.payment_system.service.PointService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PointController {

	private final PointService pointService;

	@GetMapping("/api/users/{userId}/points")
	public ResponseEntity<GetPointsResponse> getUserPoints(@PathVariable Long userId) {

		GetPointsResponse response = pointService.getUserTotalPoints(userId);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}


}
