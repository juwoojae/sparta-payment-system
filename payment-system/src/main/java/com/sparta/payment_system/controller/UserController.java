// package com.sparta.payment_system.controller;
//
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
//
// import com.sparta.payment_system.entity.User;
// import com.sparta.payment_system.repository.UserRepository;
//
// @RestController
// @RequestMapping("/api")
// public class UserController {
//
// 	@Autowired
// 	private UserRepository userRepository;
//
// 	// 인증 / 인가 기능 추가 후 컨트롤러 이관 예정
// 	@PostMapping("/user")
// 	public User createUser(@RequestParam String email,
// 		@RequestParam String passwordHash,
// 		@RequestParam(required = false) String name) {
// 		User user = new User(email, passwordHash, name);
// 		return userRepository.save(user);
// 	}
// }