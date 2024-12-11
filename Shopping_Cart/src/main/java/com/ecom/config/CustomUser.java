package com.ecom.config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecom.model.UserDtls;

public class CustomUser implements UserDetails {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private UserDtls user;
	
	public CustomUser(UserDtls user)
	{
		super();
		this.user=user;	
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		SimpleGrantedAuthority authority=new SimpleGrantedAuthority(user.getRole());
		return Arrays.asList(authority);
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getEmail();
	}
	public boolean isEnabled()
	{
		return user.getIsEnable();
	}
	
	public boolean isAccountNonLocked()
	{
		return user.getAccountNonLocked();
	}
	
	
}
