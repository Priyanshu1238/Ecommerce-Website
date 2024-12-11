package com.ecom.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ecom.model.UserDtls;
import com.ecom.repository.UserRepository;
import com.ecom.service.UserService;
import com.ecom.utils.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler {

	@Autowired
	private UserRepository ur;
	
	@Autowired
	private UserService us;
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
		String email=request.getParameter("username");
		
		UserDtls userdtls=ur.findByEmail(email);
		if(userdtls!=null) {
		if(userdtls.getIsEnable())
		{
			
			if(userdtls.getAccountNonLocked())
			{
				if(userdtls.getFailedAttempt()<AppConstant.ATTEMPT_TIME) {
				
					us.increaseFailedAttempt(userdtls);
					
				}else {
					us.userAccountLock(userdtls);
					exception=new LockedException("Your account is locked || failed attempt 3");
				}
				
			}else {
				
				if(us.unlockAccountTimeExpired(userdtls))
				{
					exception=new LockedException("Your account is unlock");
				}else {
					exception=new LockedException("your account is locked");
				}
				
			}
			
			
		}else {
			exception=new LockedException("your account is inactive");
		}
		}else {
			exception=new LockedException("your email and password is invalid");

		}
		
		
		
		
		super.setDefaultFailureUrl("/signin?error");
		super.onAuthenticationFailure(request, response, exception);
	}

}
