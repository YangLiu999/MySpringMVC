package com.woniuxy.myspringmvc.controller;

import com.woniuxy.myspringmvc.annotation.Controller;
import com.woniuxy.myspringmvc.annotation.RequestMapping;

@RequestMapping("/goods")
@Controller
public class GoodsController {
	@RequestMapping("/show")
	public void show(){
		System.out.println("show方法执行");
	}
}
