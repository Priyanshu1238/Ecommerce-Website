package com.ecom.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.Cart;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.CartService;

@Service
public class CartServiceimpl implements CartService{

	@Autowired
	private CartRepository cartrepo;
	
	@Autowired
	private UserRepository ur;
	
	@Autowired
	private ProductRepository pr;
	
	@Override
	public Cart saveCart(Integer productId, Integer userId) {
		// TODO Auto-generated method stub
		
		UserDtls userdtls=ur.findById(userId).get();
		
		Product product=pr.findById(productId).get();
		
		Cart cartstatus=cartrepo.findByProductIdAndUserId(productId, userId);
		
		Cart cart=null;
		
		if(ObjectUtils.isEmpty(cartstatus)) {
		
			cart=new Cart();
			cart.setProduct(product);
			cart.setUser(userdtls);
			cart.setQuantity(1);
			cart.setTotalPrice(1*product.getDiscountPrice());
			
			
		}else {
			cart=cartstatus;
			cart.setQuantity(cart.getQuantity()+1);
			cart.setTotalPrice(cart.getQuantity()*cart.getProduct().getDiscountPrice());
			
		}
		Cart saveCart=cartrepo.save(cart);
		
		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {
		// TODO Auto-generated method stub
		
		List<Cart> carts=cartrepo.findByUserId(userId);
		
		Double totalOrderPrice=0.0;
		List<Cart> updateCarts=new ArrayList<>();
		for(Cart c:carts)
		{
			Double totalPrice=(c.getProduct().getDiscountPrice()*c.getQuantity());
			c.setTotalPrice(totalPrice);
			
			totalOrderPrice+=totalPrice;
		
			c.setTotalOrderAmount(totalOrderPrice);
			updateCarts.add(c);
		}
		
		
		//carts.get(0).setTotalPrice(totalPrice);
		
		
		
		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {
		// TODO Auto-generated method stub
		
		Integer countbyuserid=cartrepo.countByUserId(userId);
		
		return countbyuserid;
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {
		// TODO Auto-generated method stub
		Integer updateQuantity;
		Cart cart=cartrepo.findById(cid).get();
		
		Product productDetails=cart.getProduct();
		
		if(sy.equalsIgnoreCase("de"))
		{
			updateQuantity=cart.getQuantity()-1;
			if(updateQuantity<=0)
			{
				cartrepo.delete(cart);
				
			}else {
				cart.setQuantity(updateQuantity);
				cartrepo.save(cart);
			}
			
		}else {
			updateQuantity=cart.getQuantity()+1;
			if(updateQuantity>productDetails.getStock())
			{
				cartrepo.delete(cart);
			}else {
			
			cart.setQuantity(updateQuantity);
			cartrepo.save(cart);}
		}
		
		
		
		
	}

	
}
