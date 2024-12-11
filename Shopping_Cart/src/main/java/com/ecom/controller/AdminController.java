package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Category;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.model.UserDtls;
import com.ecom.service.CategoryServices;
import com.ecom.service.OrderService;
import com.ecom.service.ProductService;
import com.ecom.service.UserService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryServices cs;
	@Autowired
	private ProductService ps;
	@Autowired
	private UserService us;
	
	@Autowired
	private OrderService os;
	
	@Autowired
	private CommonUtil cm;
	
	@ModelAttribute
	public void getUserDetails(Principal p,Model m)
	{
		if(p!=null)
		{
			String email=p.getName();
			UserDtls ud=us.getUserByEmail(email);
			m.addAttribute("user", ud);
		}
		List<Category> allactivecategory=cs.getAllActiveCategory();
		m.addAttribute("categorys", allactivecategory);
	}
	
		@GetMapping("/")
		public String index() {
			return "admin/index";
		}
		
		@GetMapping("/loadAddProduct")
		public String loadAddProduct(Model m) {
			
			List<Category> categories=cs.getAllCategory();
			m.addAttribute("categories",categories);
			
			
			return "admin/add_product";
		}
		
		@GetMapping("/category")
		public String category(Model m) {
			
			m.addAttribute("categorys", cs.getAllCategory());
			
			return "admin/category";
		}
		
		@PostMapping("/savecategory")
		public String savecategory(@ModelAttribute Category category,@RequestParam boolean isActive,@RequestParam("file") MultipartFile file,HttpSession session) throws IOException{
			
			String imageName=file!=null?file.getOriginalFilename():"default.jpg";
			
			category.setImagename(imageName);
			category.setIsActive(isActive);
			if(cs.existCategory(category.getName())) {
				session.setAttribute("errorMsg","Category name already exists");
			}
			else {
			Category savecategory= cs.savecategory(category);
			
			
			if(ObjectUtils.isEmpty(savecategory))
			{
				session.setAttribute("errorMsg","Not saved ! internal server error");
			}
			else
			{
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
			System.out.println(path);
				
				session.setAttribute("succMsg","Saved sucessfully");
			}
			
			}
			return "redirect:/admin/category";
		}
		
		@GetMapping("/deleteCategory/{id}")
		public String deleteCategory(@PathVariable int id,HttpSession session )
		{
			Boolean dc=cs.deleteCategory(id);
			
			if(dc)
			{
				session.setAttribute("succMsg", "category delete successfully");
			}
			else
			{
				session.setAttribute("errMsg", "category not get deleted");
			}
			
			return "redirect:/admin/category";
		}
		
		
		@GetMapping("/loadEditCategoryurl/{id}")
		public String loadEditCategory(@PathVariable int id,Model m)
		{
			m.addAttribute("category", cs.getCategoryById(id));
			return "/admin/edit_category";
		}
		
		
		@PostMapping("/updateCategory")
		public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,HttpSession session) throws IOException {
			
			Category Oldcategory=cs.getCategoryById(category.getId());
			String filename=file.isEmpty()?Oldcategory.getImagename():file.getOriginalFilename();
			if(!ObjectUtils.isEmpty(category))
			{
				Oldcategory.setName(category.getName());
				Oldcategory.setIsActive(category.getIsActive());
				Oldcategory.setImagename(filename);
				
			}
			
			Category updateCategory=cs.savecategory(Oldcategory);
			
			if(!ObjectUtils.isEmpty(updateCategory))
			{
			
				
				if(!file.isEmpty())
					
				{
					
					
					File saveFile=new ClassPathResource("static/img").getFile();
					
					Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());
					
					Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
					
					
					
				}
				
				
				session.setAttribute("succMsg", "Category update sucessfully");
			}
			else
			{
				session.setAttribute("errorMsg", "something wrong on server");
			}
			
			
			
			return "redirect:/admin/loadEditCategoryurl/"+category.getId();
		}
		
		
		@PostMapping("/saveProduct")
		public String saveProduct(@ModelAttribute Product product,HttpSession session, @RequestParam("file") MultipartFile image) throws IOException
		{
			String imagename=image.isEmpty()?"default.jpg":image.getOriginalFilename();
			
			product.setImage(imagename);
			
			product.setDiscount(0);
			
			product.setDiscountPrice(product.getPrice());
			
			Product saveProduct=ps.saveProduct(product);			
			
			if(!ObjectUtils.isEmpty(saveProduct))
			{
				
				
				File saveFile=new ClassPathResource("static/img").getFile();
				
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());
				
				Files.copy(image.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				
				
				System.out.println(path);
				
				
				
				
				session.setAttribute("succMsg", "Product save successfully");
			}
			else {
				session.setAttribute("errorMsg","something went wrong");
			}
			
			return "redirect:/admin/loadAddProduct";
		}
		
		
		@GetMapping("/products")
		public String loadViewProduct(Model m,@RequestParam(defaultValue="") String ch)
		{
			List<Product> searchProducts=null;
			
			if(ch!=null && ch.length()>0)
			{
				searchProducts=ps.searchProduct(ch);
			}else {
				searchProducts=ps.getAllProduct();
			}
			
			
			
			m.addAttribute("products", searchProducts);
			return "admin/products";
		}
		
		@GetMapping("/deleteProduct/{id}")
		public String deleteProduct(@PathVariable int id,HttpSession session)
		{
			Boolean status=ps.deleteProduct(id);
			
			if(status)
			{
				session.setAttribute("succMsg", "Product deleted successfully");
			}
			else
			{
				session.setAttribute("errorMsg", "Something went wrong");
			}
			
			return "redirect:/admin/products";
		}
		
		
		
		@GetMapping("/editProduct/{id}")
		public String editProduct(@PathVariable int id,Model m)
		{
			m.addAttribute("product",ps.getProductById(id));
			m.addAttribute("categories", cs.getAllCategory());
			return "admin/edit_product";
		}
		
		
		@PostMapping("/updateProduct")
		public String updateProduct(@ModelAttribute Product product,HttpSession session,@RequestParam("file") MultipartFile image,Model m) throws IOException
		{
			if(product.getDiscount()<0 || product.getDiscount()>100)
			{
				session.setAttribute("errorMsg", "Invalid discount");
			}
			
			else {
			
			
			Product updateProduct=ps.updateProduct(product, image);
			
			if(!ObjectUtils.isEmpty(updateProduct))
			{
				session.setAttribute("succMsg", "Product edited sucessfully");
			}
			else
			{
				session.setAttribute("errorMsg","Something wrong occured");
			}
			
			}
			return "redirect:/admin/editProduct/"+product.getId();
		}
		
		
		@GetMapping("/users")
		public String getAllUsers(Model m)
		{
			List<UserDtls> users=us.getAllUser("ROLE_USER");
			m.addAttribute("users", users);
			return "/admin/users";
		}
		
		@GetMapping("/updateStatus")
		public String updateUserAccountStatus(HttpSession session,@RequestParam Boolean status,@RequestParam Integer id)
		{
			Boolean f=us.updateAccountStatus(id,status);
			
			if(f)
			{
				session.setAttribute("succMsg", "Account Status Updated");
			}
			else
			{
				session.setAttribute("errorMsg", "Something Went Wrong");
			}
			
			
			return "redirect:/admin/users";
		}
		
		@GetMapping("/ordersModule")
		public String gtAllOrders(Model m) {
			List<ProductOrder> orders=os.getAllOrders();
			
			m.addAttribute("orders", orders);
			
			m.addAttribute("srch",false);

			return "/admin/orders";
		}
		
		
		@PostMapping("/update-order-status")
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
			
			
			if( !ObjectUtils.isEmpty(updateOrder)) {
				session.setAttribute("succMsg", "Order Updated successfully");
			}else {
				session.setAttribute("errorMsg", "Something went wrong");
			}
			
			return "redirect:/admin/ordersModule";
		}
		
		
		
		@GetMapping("/search-order")
		public String searchProduct(@RequestParam String orderId,Model m,HttpSession session)
		{
			
			ProductOrder order=os.getOrderByOrderId(orderId.trim());
			
			if(ObjectUtils.isEmpty(order))
			{
				session.setAttribute("errorMsg", "Incorrect orderId");
				m.addAttribute("orderdetails", null);
				
				gtAllOrders(m);

			
				

			}else {
				m.addAttribute("orderdetails", order);
				m.addAttribute("srch",true);
			}
			
			
			return "/admin/orders";
		}
		
	
		
}


















