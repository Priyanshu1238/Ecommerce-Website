package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

public interface OrderService {

	
	public void saveOrder(Integer userid,OrderRequest orderRequest) throws Exception;
	
	public List<ProductOrder> getOrderByUser(Integer userId);
	
	public ProductOrder UpdateOrderStatus(Integer id,String status);
	
	public List<ProductOrder> getAllOrders();
	
	public Page<ProductOrder> getAllOrdersPagination(Integer pageNo,Integer pageSize);
	
	public ProductOrder getOrderByOrderId(String id);
	
	
}
