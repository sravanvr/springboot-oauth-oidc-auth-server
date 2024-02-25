package io.github.auth.backend.auth.service;

import io.github.auth.backend.auth.registration.dao.PrivilegeRepository;
import io.github.auth.backend.auth.registration.dao.RoleRepository;
import io.github.auth.backend.auth.registration.dao.UserRepository;
import io.github.auth.backend.auth.registration.model.Role;
import io.github.auth.backend.auth.registration.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserService {

	@Autowired
	private UserRepository repo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

	public User findOrCreateUser(Jwt jwt ) {
		User existUser = repo.findByEmail(jwt.getSubject());

        Role userRole = roleRepository.findByName("ROLE_USER");

		if (existUser == null) {
            User user = new User();
            user.setFirstName(jwt.getClaims().get("name").toString());
            user.setLastName(jwt.getClaims().get("family_name").toString());
            user.setPassword(passwordEncoder.encode("test"));
            user.setEmail(jwt.getClaims().get("email").toString());
            user.setRoles(Arrays.asList(userRole));
            user.setEnabled(true);
            userRepository.save(user);

			System.out.println("Created new user: " + jwt.getSubject());
            return user;
		}
        return existUser;
	}
}
