package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Cart;
import com.ecom.model.OrderAddress;
import com.ecom.model.OrderRequest;
import com.ecom.model.Product;
import com.ecom.model.ProductOrder;
import com.ecom.repository.CartRepository;
import com.ecom.repository.ProductOrderRepository;
import com.ecom.repository.ProductRepository;
import com.ecom.service.OrderService;
import com.ecom.utils.CommonUtil;
import com.ecom.utils.OrderStatus;

@Service
public class OrderServiceImpl implements OrderService{

	@Autowired
	private ProductOrderRepository or;
	
	@Autowired
	private ProductRepository pr;
	
	@Autowired
	private CartRepository cartrepo;
	
	@Autowired
	private CommonUtil cm;
	
	@Override
	public void saveOrder(Integer userid,OrderRequest orderRequest) throws Exception {
		// TODO Auto-generated method stub
		
		List<Cart> carts=cartrepo.findByUserId(userid);
		
		for(Cart cart:carts)
		{
			
			
			ProductOrder order=new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());
			
			order.setProduct(cart.getProduct());
			
			order.setPrice(cart.getProduct().getDiscountPrice());
			
			order.setQuantity(cart.getQuantity());
			
			Product changedProduct=cart.getProduct();
			
			
			changedProduct.setStock(changedProduct.getStock()-cart.getQuantity());
			pr.save(changedProduct);
			
			
			order.setUser(cart.getUser());
			
			order.setStatus(OrderStatus.IN_PROGRESS.getName());
			order.setPaymentType(orderRequest.getPaymentType());
			
			OrderAddress addr=new OrderAddress();
			
			addr.setFirstName(orderRequest.getFirstName());
			addr.setLastName(orderRequest.getLastName());
			addr.setEmail(orderRequest.getEmail());
			addr.setMobileNo(orderRequest.getMobileNo());
			addr.setAddress(orderRequest.getAddress());
			addr.setCity(orderRequest.getCity());
			addr.setState(orderRequest.getState());
			addr.setPincode(orderRequest.getPincode());
			
			order.setOrderAddress(addr);
			
			ProductOrder saveOrder=or.save(order);
			
			cm.sendMailForProductOrder(saveOrder,"Placed");
			
		}
		
		
		
		
		
	}

	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {
		// TODO Auto-generated method stub
		
		List<ProductOrder> orders=or.findByUserId(userId);
		
		
		return orders;
	}

	@Override
	public ProductOrder UpdateOrderStatus(Integer id, String status) {
		// TODO Auto-generated method stub
		Optional<ProductOrder> findById=or.findById(id);
		
		if(findById.isPresent())
		{
			ProductOrder productOrder=findById.get();
			productOrder.setStatus(status);
			ProductOrder updateOrder=or.save(productOrder);
			return updateOrder;
		}
		
		
		return null;
	}

	@Override
	public List<ProductOrder> getAllOrders() {
		// TODO Auto-generated method stub
		
		return or.findAll();

	}

	@Override
	public ProductOrder getOrderByOrderId(String id) {
		// TODO Auto-generated method stub
		
		return or.findByOrderId(id);
		
	}
	

}
