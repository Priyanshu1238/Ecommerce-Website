package com.ecom.utils;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.model.ProductOrder;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;


@Component
public class CommonUtil {

	@Autowired
	private  JavaMailSender mailsender;
	
	public  Boolean sendMail(String url, String reciepentemail) throws UnsupportedEncodingException, MessagingException
	{
		
		MimeMessage message=mailsender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		
		helper.setFrom("2chillers2@gmail.com","Shoping Cart");
		helper.setTo(reciepentemail);
		String content="<p>Hello,</p>"+"<p>You have requeted to reset your password.</p>"
					+"<p>Click the link below to change your password:</p>"+
					"<p><a href=\""
					+url
					+"\">Change my password</a></p>";
		helper.setSubject("Password Reset");
		helper.setText(content,true);
		mailsender.send(message);
		
		
		return true;
	}

	public static String generateUrl(HttpServletRequest request) {
		// TODO Auto-generated method stub
		
		 String siteurl=request.getRequestURL().toString();
		return  siteurl.replace(request.getServletPath(), "");
		
		 
		
	}
	
	
	String msg=null;
	
	
	public Boolean sendMailForProductOrder(ProductOrder order,String status) throws Exception
	{
		
		String msg="<p>Hi [[name]],</p>"
				+ "<p>Thank you , </p>"+
					"<p>Product Details : [[ProductName]]</p>"
				+"<p>Order Id : [[orderId]]</p>"
				+"<p>Category : [[category]]</p>"
					+"<p>Quantity : [[quantity]]</p>"
				+"<p>Price :[[price]]</p>"
					+"<p>Total Price :[[totalPrice]]"
					+"<p> has successfully <b>[[orderStatus]]</b></p>";
		
		
		
		
		
		
		
		
		
		MimeMessage message=mailsender.createMimeMessage();
		MimeMessageHelper helper=new MimeMessageHelper(message);
		
		helper.setFrom("2chillers2@gmail.com","Shoping Cart");
		helper.setTo(order.getOrderAddress().getEmail());
		
		Double totalprice=(order.getPrice()*order.getQuantity());

	
		msg=msg.replace("[[name]]", order.getOrderAddress().getFirstName());
		msg=msg.replace("[[orderStatus]]", status);
		
		msg=msg.replace("[[ProductName]]", order.getProduct().getTitle());
		msg=msg.replace("[[category]]", order.getProduct().getCategory());
		msg=msg.replace("[[quantity]]", order.getQuantity().toString());
		msg=msg.replace("[[price]]", order.getPrice().toString());
		msg=msg.replace("[[totalPrice]]", totalprice.toString());
		
		
		//Latest Update
		msg=msg.replace("[[orderId]]",order.getOrderId());
		
		
		
		
		
		
		helper.setSubject("Product Ordered Status");
		helper.setText(msg,true);
		mailsender.send(message);
		
		
		return true;
		
		
		
	}
}
