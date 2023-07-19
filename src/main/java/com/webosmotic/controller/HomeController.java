package com.webosmotic.controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webosmotic.entity.User;
import com.webosmotic.repository.UserRepository;
import com.webosmotic.roles.UserRole;

@RestController
@RequestMapping("/user")
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@PostMapping("/create")
	public String create(@RequestBody User user) {
		user.setRoles(UserRole.DEFAULT_ROLE);
		String password = bCryptPasswordEncoder.encode(user.getPassword());
		user.setPassword(password);
		userRepository.save(user);
		return user.getUsername();
	}

	@GetMapping
	public List<User> getAllUser() {
		List<User> findAll = userRepository.findAll();
		return findAll;
	}

	@GetMapping("/access/{id}/{roles}")
	@PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_MODERATOR')")
	public String giveAccessToUser(@PathVariable int id, @PathVariable String roles, Principal principal) {
		User user = userRepository.findById(id).get();
		List<String> activeRoles = getRolesByLoggedInUser(principal);
		if (activeRoles.contains(roles)) {
			roles = user.getRoles() + "," + roles;
			user.setRoles(roles);
		}
		userRepository.save(user);
		return "Hi" +" "+ user.getUsername() + " " + "New role assign to you by" +" "+ principal.getName();

	}

	private List<String> getRolesByLoggedInUser(Principal principal) {
		String roles = getLoggedInuser(principal).getRoles();

		List<String> assignRoles = Arrays.stream(roles.split(",")).collect(Collectors.toList());
		if (assignRoles.contains(UserRole.ADMIN_ACCESS)) {
			return Arrays.stream(UserRole.ADMIN_ACCESS).collect(Collectors.toList());
		}
		if (assignRoles.contains(UserRole.MODERATOR_ACCESS)) {
			return Arrays.stream(UserRole.MODERATOR_ACCESS).collect(Collectors.toList());
		}
		return Collections.emptyList();

	}

	private User getLoggedInuser(Principal principal) {
		return userRepository.findByUsername(principal.getName()).get();
	}

}
