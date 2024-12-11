package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Cart;
import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.UserDtls;
import com.ecom.service.CartService;
import com.ecom.service.CategoryServices;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.utils.CommonUtil;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private CartService cartservice;
	
	@Autowired
	private ProductService ps;
	
	@Autowired
	private CategoryServices cs;
	
	@Autowired
	private UserService us;
	
	@Autowired
	private CommonUtil cu;
	
	@Autowired
	private BCryptPasswordEncoder pe;
	
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
	
	
		@GetMapping("/")
		public String index() {
			return "index";
		}
		
		@GetMapping("/signin")
		public String login() {
			return "login";
		}
		
		@GetMapping("/register")
		public String register() {
			return "register";
		}
		
		@GetMapping("/product")
		public String product(Model m,@RequestParam(value="category",defaultValue="") String category,
				@RequestParam(name="pageNo",defaultValue ="0") Integer pageNo,
				@RequestParam(name="pageSize",defaultValue="2") Integer pageSize)
		{
			
			
			List<Category> categories=cs.getAllActiveCategory();
			m.addAttribute("categories", categories);
			m.addAttribute("paramValue",category);
			
			
//			List<Product> product=ps.getAllActiveProduct(category);
//			m.addAttribute("product", product);
			
			Page<Product>page=ps.getAllActiveProductPagination(pageNo,pageSize,category);
			
			List<Product> products=page.getContent();
			
			m.addAttribute("product", products);
			
			m.addAttribute("productSize",products.size());
			
			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize",pageSize);
			m.addAttribute("totalElements",page.getTotalElements());
			m.addAttribute("totalPages",page.getTotalPages());
			m.addAttribute("isFirst",page.isFirst());
			m.addAttribute("isLast",page.isLast());
			
			
			
			return "product";
		}
		
		@GetMapping("/view_product/{id}")
		public String singleproduct(@PathVariable int id,Model m) {
			
			Product productById=ps.getProductById(id);
			m.addAttribute("product",productById);
			return "view_product";
		}
		
		@PostMapping("/saveuser")
		public String saveuser(HttpSession session,@ModelAttribute UserDtls user,@RequestParam("img") MultipartFile file) throws IOException
		{
			String imgname=file.isEmpty()?"default.jpg":file.getOriginalFilename();
			user.setProfileImage(imgname);
			UserDtls saveuser=us.saveUser(user);
			if(!ObjectUtils.isEmpty(saveuser))
			{
				if(!file.isEmpty())
				{
					
					File saveFile=new ClassPathResource("static/img").getFile();
					
					Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
					
					
					System.out.println(path);
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					
					
					
				}
				session.setAttribute("succMsg", "User registered successfully");
			}else {
				session.setAttribute("errorMsg","something went wrong");
			}
			
			
			return "redirect:/register";
		}
		
		//forget Passsword
		
		@GetMapping("/forgot-password")
		public String showForgotPassword()
		{
			return "forgot_password.html";
		}
		
		
		@PostMapping("/forgot-password")
		public String processForgotPassword(@RequestParam String email,HttpSession session,HttpServletRequest request) throws UnsupportedEncodingException, MessagingException
		{
			UserDtls userbyemail=us.getUserByEmail(email);
			if(ObjectUtils.isEmpty(userbyemail))
			{
				session.setAttribute("errorMsg", "Invalid email");
			}
			else
			{
				String resetToken=UUID.randomUUID().toString();
				us.updateUserResetToken(email,resetToken);
				
				//Generate  URL :http://localhost:8080/reset-pasword?token=shjdsvghdjndbdnmfbd
				
				String url=CommonUtil.generateUrl(request)+"/reset-password?token="+resetToken;
				
				
				Boolean sendmail=cu.sendMail(url,email);
				if(sendmail) {
					session.setAttribute("succMsg", "Please check your mail");
				}else {
					session.setAttribute("errorMsg", "Something went wrong");

				}
			}
			
			
			return "redirect:/forgot-password";
		}
		
		
		
		
		@GetMapping("/reset-password")
		public String showResetPassword(@RequestParam String token,HttpSession session,Model m)
		{
			UserDtls userbytoken=us.getUserByToken(token);
			
			if(userbytoken==null)
			{
				m.addAttribute("Msg", "Yout link is invalid");
				return "message";
			}
			m.addAttribute("token",token);
			return "reset_password.html";
		}
		
		
		
		@PostMapping("/reset-password")
		public String ResetPassword(@RequestParam String token,@RequestParam String password,HttpSession session,Model m)
		{
			UserDtls userbytoken=us.getUserByToken(token);
			
			if(userbytoken==null)
			{
				m.addAttribute("Msg", "Yout link is invalid");
				return "message";
			}else {
				userbytoken.setPassword(pe.encode(password));
				userbytoken.setReset_token(null);
				us.updateUser(userbytoken);
				//session.setAttribute("Msg", "Password change sucessfully");
				m.addAttribute("Msg", "Password change sucessfully");
				return "message";
			}
			
			
			
			
		}
		
		
		
		//user
		
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
		
		
		@GetMapping("/search")
		public String searchProduct(@RequestParam String ch,Model m)
		{
			List<Product> searchproduct=ps.searchProduct(ch);
			
			m.addAttribute("product",searchproduct);
			
			List<Category> categories=cs.getAllActiveCategory();
			
			m.addAttribute("categories", categories);
			
			return "product";
		}
		
	
		
}
