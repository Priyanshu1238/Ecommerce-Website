package com.ecom.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
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
import com.ecom.utils.AppConstant;

@Service
public class UserServiceimpl implements UserService {

	@Autowired
	private UserRepository ur;
	
	@Autowired
	private PasswordEncoder pe;
	
	@Override
	public UserDtls saveUser(UserDtls user) {
		// TODO Auto-generated method stub
		
		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		//user.setLockTime(null);
		String encodedpass=pe.encode(user.getPassword());
		user.setPassword(encodedpass);
		UserDtls saveuser=ur.save(user);
		return saveuser;
	}

	@Override
	public UserDtls getUserByEmail(String email) {
		// TODO Auto-generated method stub
		UserDtls user=ur.findByEmail(email);
		return user;
	}

	@Override
	public List<UserDtls> getAllUser(String role) {
		// TODO Auto-generated method stub
		
		return ur.findByRole(role);
	}

	@Override
	public Boolean updateAccountStatus(Integer id, Boolean Status) {
		// TODO Auto-generated method stub
		Optional<UserDtls> userid = ur.findById(id);
		if(userid!=null)
		{
			UserDtls userdtls=userid.get();
			userdtls.setIsEnable(Status);
			ur.save(userdtls);
			return true;
			
		}
		return false;
	}

	@Override
	public void increaseFailedAttempt(UserDtls user) {
		// TODO Auto-generated method stub
		
		int attempt =user.getFailedAttempt()+1;
		user.setFailedAttempt(attempt);
		ur.save(user);
		
	}

	@Override
	public void userAccountLock(UserDtls user) {
		// TODO Auto-generated method stub
		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		ur.save(user);
		
	}

	@Override
	public Boolean unlockAccountTimeExpired(UserDtls user) {
		// TODO Auto-generated method stub
		
		long lockTime=user.getLockTime().getTime();
		long unlockTime=lockTime+AppConstant.UNLOCK_DURATION_TIME;
		
		long curTime=System.currentTimeMillis();
		
		if(unlockTime<curTime)
		{
			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			ur.save(user);
			return true;
		}
		
		
		return false;
	}

	@Override
	public void resetAttempt(int userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {
		// TODO Auto-generated method stub
		UserDtls findbyemail=ur.findByEmail(email);
		findbyemail.setReset_token(resetToken);
		ur.save(findbyemail);
	}

	@Override
	public UserDtls getUserByToken(String token) {
		// TODO Auto-generated method stub
		return ur.findByResetToken(token);
	
	}

	@Override
	public UserDtls updateUser(UserDtls user) {
		// TODO Auto-generated method stub
		
		return ur.save(user);
		
		
	}

	@Override
	public UserDtls updateUserProfile(UserDtls user,MultipartFile file) throws IOException {
		// TODO Auto-generated method stub
		
		UserDtls dbUser=ur.findById(user.getId()).get();
		if(!file.isEmpty())
		{
			dbUser.setProfileImage(file.getOriginalFilename());
		}
		
		
		if(!ObjectUtils.isEmpty(dbUser))
		{
			dbUser.setName(user.getName());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			
			dbUser=ur.save(dbUser);
		}
		
		if(!file.isEmpty())
		{
			
			File saveFile=new ClassPathResource("static/img").getFile();
			
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
			
			
			System.out.println(path);
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
			
			
			
			
		}
		
		return dbUser;
	}

	@Override
	public UserDtls saveAdmin(UserDtls user) {
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		//user.setLockTime(null);
		String encodedpass=pe.encode(user.getPassword());
		user.setPassword(encodedpass);
		UserDtls saveuser=ur.save(user);
		return saveuser;
	}

	@Override
	public Boolean existsEmail(String email) {
		// TODO Auto-generated method stub
		
		return ur.existsByEmail(email);
	}

}
