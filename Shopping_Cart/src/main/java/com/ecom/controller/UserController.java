package com.ecom.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryServices;
import com.ecom.service.OrderService;
import com.ecom.service.UserService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService us;
	@Autowired
	private CategoryServices cs;
	
	@Autowired
	private CartService cartservice;
	
	@Autowired
	private OrderService os;
	
	@Autowired
	private CommonUtil cm;
	
	@Autowired
	private PasswordEncoder pe;
	
	@GetMapping("/")
	public String home()
	{
		return "user/home";
		
	}
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m)
	{
		if(p!=null)
		{
			String email=p.getName();
			UserDtls ud=us.getUserByEmail(email);
			m.addAttribute("user", ud);
			Integer countcart=cartservice.getCountCart(ud.getId());
			m.addAttribute("countcart",countcart);
		}
		List<Category> allactivecategory=cs.getAllActiveCategory();
		m.addAttribute("categorys", allactivecategory);
		
	}
	
	@GetMapping("/addcart")
	public String addToCart(@RequestParam Integer pid,@RequestParam Integer uid,HttpSession session)
	{
		Cart saveCart=cartservice.saveCart(pid, uid);
		if(ObjectUtils.isEmpty(saveCart)) {
			session.setAttribute("errorMsg", "Fail to add product");
		}else {
			session.setAttribute("succMsg", "Product added sucessfully");
		}
		return "redirect:/view_product/"+pid;
	}
	
	@GetMapping("/cart")
	public String loadCartPage(Principal p,Model m)
	{
		
	 UserDtls user=getLoggedInUserDetails(p);
	 
	 List<Cart> carts=cartservice.getCartsByUser(user.getId());
	 m.addAttribute("carts", carts);
	 if(carts.size()>0)
	 {
	 Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderAmount();
	 
	 m.addAttribute("totalOrderPrice",totalOrderPrice );
		
	 }
		
		return "/user/cart";
	}
	
	@GetMapping("/cartQuantityUpdate")
	public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid)
	{
		cartservice.updateQuantity(sy,cid);
		return "redirect:/user/cart";
	}
	
	

	private UserDtls getLoggedInUserDetails(Principal p) {
		// TODO Auto-generated method stub
		
		String email=p.getName();
		UserDtls userdtls=us.getUserByEmail(email);
		
		return userdtls;
	}
	
	
	@GetMapping("/orders")
	public String OrderPage(Principal p, Model m)
	{
		
		
		UserDtls user=getLoggedInUserDetails(p);
		 
		 List<Cart> carts=cartservice.getCartsByUser(user.getId());
		 m.addAttribute("carts", carts);
		 if(carts.size()>0)
		 {
		 Double OrderPrice=carts.get(carts.size()-1).getTotalOrderAmount();
		 Double totalOrderPrice=carts.get(carts.size()-1).getTotalOrderAmount()+250 +100;
		 m.addAttribute("OrderPrice", OrderPrice);
		 m.addAttribute("totalOrderPrice",totalOrderPrice );
		 
		 }
		 
		 
		 m.addAttribute("userDetails", user);
		return "/user/order";
	}
	
	@PostMapping("/save-order")
	public String saveOrder(@ModelAttribute OrderRequest request,Principal p) throws Exception
	{
		//System.out.println(request);
		
		UserDtls user=getLoggedInUserDetails(p);
		os.saveOrder(user.getId(), request);
		
		
		
		return "redirect:/user/success";
	}
	@GetMapping("/success")
	public String loadSuccess()
	{
		return "/user/success";
	}
	
	
	@GetMapping("/user-orders")
	public String myOrder(Model m,Principal p)
	{
		UserDtls loginuser=getLoggedInUserDetails(p);
		List<ProductOrder> orders=os.getOrderByUser(loginuser.getId());
		m.addAttribute("orders", orders);
		return "/user/my_orders";
	}
	
	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,HttpSession session)
	{
		
		//os.UpdateOrderStatus(id, st);
		
		OrderStatus[] orderStatus=OrderStatus.values();
		String status=null;
		for(OrderStatus orderSt:orderStatus)
		{
			if(orderSt.getId().equals(st)) {
				status=orderSt.getName();
			}
		}
		ProductOrder updateOrder=os.UpdateOrderStatus(id, status);
		
		try {
			cm.sendMailForProductOrder(updateOrder, status);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		if(!ObjectUtils.isEmpty(updateOrder)) {
			session.setAttribute("succMsg", "Order Updated successfully");
		}else {
			session.setAttribute("errorMsg", "Something went wrong");
		}
		
		return "redirect:/user/user-orders";
	}
	
	
	@GetMapping("/profile")
	public String profile()
	{
		return "user/profile";
	}
	
	
	@PostMapping("/update-profile")
	public String updateProfile(HttpSession session,@ModelAttribute UserDtls user,@RequestParam MultipartFile img) throws IOException
	{
		
		UserDtls updateprofiledata=us.updateUserProfile(user, img);
		if(ObjectUtils.isEmpty(updateprofiledata))
		{
			session.setAttribute("errorMsg", "Profile Not Updated");

		}
		else
		{
			session.setAttribute("succMsg", "Profile Updated successfully");
	
		}
		
		return "redirect:/user/profile";
	}
	
	@PostMapping("/change-password")
	public String chnagePassword(@RequestParam String newPassword,@RequestParam String currentPassword,Principal p,HttpSession session)
	{
		UserDtls getLoggedInUserDetails=getLoggedInUserDetails(p);
		
		boolean matches= pe.matches(currentPassword, getLoggedInUserDetails.getPassword());
		
		if(matches)
		{
			String encodePassword=pe.encode(newPassword);
			getLoggedInUserDetails.setPassword(encodePassword);
			UserDtls userupdated=us.updateUser(getLoggedInUserDetails);
			
			if(ObjectUtils.isEmpty(userupdated))
			{
				session.setAttribute("errorMsg", "Something went wrong");

				
			}else {
				session.setAttribute("succMsg", "Reset Password successfully");

			}
			
		}else {
			session.setAttribute("errorMsg", "Current Password is wrong");

		}
		return "redirect:/user/profile";
	}
	
	
}
