package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDtls saveUser(UserDtls user) {
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		String encodePassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodePassword);
		UserDtls saveUser = userRepository.save(user);
		return saveUser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {

		return userRepository.findByEmail(email);
	}

	@Override
	public List<UserDtls> getUsers(String role) {

		return userRepository.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean status) {
		Optional<UserDtls> findByuser = userRepository.findById(id);

		if (findByuser.isPresent()) {
			UserDtls userDtls = findByuser.get();
			userDtls.setIsEnable(status);
			userRepository.save(userDtls);
			return true;
		}
		return false;
	}

	@Override
	public UserDtls updateUser(UserDtls user) {

		return userRepository.save(user);
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user, MultipartFile img) {
		UserDtls dbUser = userRepository.findById(user.getId()).get();

		if (!img.isEmpty()) {
			dbUser.setProfileImage(img.getOriginalFilename());
		}

		if (!ObjectUtils.isEmpty(dbUser)) {
			dbUser.setName(user.getName());
			dbUser.setMobilePhone(user.getMobilePhone());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser = userRepository.save(dbUser);
		}
		try {
			if (!img.isEmpty()) {

				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Path.of(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ img.getOriginalFilename());
				// System.out.print(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbUser;
	}

}
