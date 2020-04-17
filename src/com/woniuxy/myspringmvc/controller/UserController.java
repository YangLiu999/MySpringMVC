package com.woniuxy.myspringmvc.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.woniuxy.myspringmvc.annotation.Controller;
import com.woniuxy.myspringmvc.annotation.RequestMapping;
import com.woniuxy.myspringmvc.annotation.ResponseBody;
import com.woniuxy.myspringmvc.annotation.ResponseForward;
import com.woniuxy.myspringmvc.annotation.ResponseRedirect;
import com.woniuxy.myspringmvc.entity.Demo;

@RequestMapping("/user")
@Controller
public class UserController {
	
	@RequestMapping("/login")
	public void login(HttpServletRequest request){
		System.out.println("login执行"+request.getRequestURI());
	}
	@RequestMapping("/register")
	public void register(HttpServletRequest response){
		System.out.println("register执行"+response);
	}
	
	@RequestMapping("/primitive")
	public void primitiveDemo(byte b,short s,int i,long l,float f,
			double d,char c,boolean bool,String str,BigDecimal 
			big,String[] strs,int[] nums){
		//客户端传入基本数据类型处理
		System.out.println(b);
		System.out.println(s);
		System.out.println(i);
		System.out.println(l);
		System.out.println(f);
		System.out.println(d);
		System.out.println(c);
		System.out.println(bool);
		System.out.println(str);
		System.out.println(big);
		for(String string:strs){
			System.out.println(string);
		}
		for (int j : nums) {
			System.out.print(j); 
		}
	}
	
	@RequestMapping("/demo")
	public void getDemo(Demo demo){
		System.out.println(demo);
	}
	
	@ResponseRedirect(true)
	@RequestMapping("/test")
	public String test(){
		
		//return "index.jsp";
		return "/MySpringMVC/index.jsp";
	}
	
	@ResponseBody
	@RequestMapping("/json")
	public List<?> jsonTest(){
		System.out.println("test");
		vi_allStudeantsPO po = new vi_allStudeantsPO(1, "张三", "男", 20, "13966666666", 2, "Java");
		vi_allStudeantsPO po1 = new vi_allStudeantsPO(1, "李四", "男", 20, "13966666666", 2, "Java");
		List<vi_allStudeantsPO> ls = new ArrayList<vi_allStudeantsPO>();
		ls.add(po);
		ls.add(po1);
		return ls;
	}
}
