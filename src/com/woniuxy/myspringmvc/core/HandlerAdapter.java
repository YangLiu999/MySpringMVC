package com.woniuxy.myspringmvc.core;
import java.io.IOException;
import java.lang.reflect.Field;
/**
 * 处理器适配器
 */
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class HandlerAdapter {
	private HandlerMapper handlerMapper;

	public HandlerAdapter(HandlerMapper handlerMapper) {
		this.handlerMapper = handlerMapper;
	}
	
	public Object adapter(HttpServletRequest request, HttpServletResponse response) {
		//设定返回值
		Object result = null;
		// 当客户端发起请求时，根据请求映射找到对应controller对象与Method对象，并执行；
		// 获取请求的映射:存储的key
		String pathInfo = request.getPathInfo();
		// System.out.println(pathInfo);
		// 根据请求映射找到对应的controller对象
		Object controller = handlerMapper.getControllerMapping().get(pathInfo);
		// 查找method对象
		Method method = handlerMapper.getMethodMapping().get(pathInfo);
		// 判断是否存在对应目标对象
		if (controller == null || method == null) {
			try {
				// 抛出异常状态信息
				response.sendError(666, controller+"请求映射未找到controller或method对象");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		//获取方法形参数量
		int methodParamCount = method.getParameterCount();
		//存储方法实参
		Object[] realMethodParam = new Object[methodParamCount];
		//获取客户端参数值：实参
		Map<String, String[]> requestParamVals = request.getParameterMap();
		//获取方法参数:形参
		Parameter[] methodParams = method.getParameters();
		//循环遍历参数
		for (int i = 0; i < methodParams.length; i++) {
			//取出每一个参数
			Parameter methodParam = methodParams[i];
			//获取参数类型
			Class<?> methodParamTypeCl = methodParam.getType();
			//判断参数类型
			//判断当前class是否为指定类型或者父类的class
			//判断目标方法是否存在request或者response参数
			if (methodParamTypeCl.isAssignableFrom(HttpServletRequest.class)) {
				realMethodParam[i] = request;
			}else if (methodParamTypeCl.isAssignableFrom(HttpServletResponse.class)) {
				realMethodParam[i] = response;
				//处理基本数据类型
			}else if (methodParamTypeCl.isPrimitive()
					||methodParamTypeCl.isAssignableFrom(String.class)
					||methodParamTypeCl.isAssignableFrom(BigDecimal.class)
					||methodParamTypeCl.isArray()){
				//根据方法参数名获取客户端传参值，并进行类型转换(String转为方法参数类型)
				//方法参数名：形参
				String methodParamName = methodParam.getName();
				//System.out.println(methodParamName);
				//根据参数名查找参数值：参数名形参和实参必须保持一致
				String[] methodParamVals = requestParamVals.get(methodParamName);
				//非空判断
				if (methodParamVals==null) {
					try {
						//直接给浏览器发送错误提示
						response.sendError(888, methodParamName+"未获取到参数值");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				//类型转换:String转为方法参数对应类型
				//将转换后的值存储到对应实参数组
				realMethodParam[i] = parse(methodParamVals,methodParamTypeCl);
			//参数类型是自定义类型:类加载不为空
			}else if (methodParamTypeCl.getClassLoader()!=null) {
				/*
				 * 获取目标类所有属性，拼接键（参数名称.属性名称）
				 * 从请求中获取参数值，赋值给变量
				 * 最终将赋值的对象赋给实参数组
				 */
				//获取所有属性
				Field[] methodParamFields = methodParamTypeCl.getDeclaredFields();
				//获取参数名
				String methodParamName = methodParam.getName();
				Object obj=null;
				try {
					//创建当前参数类对象
					obj = methodParamTypeCl.newInstance();
				} catch (Exception e1) {
					e1.printStackTrace();
				} 
				for (int j = 0; j < methodParamFields.length; j++) {
					//属性名
					String methodParamFieldName = methodParamFields[j].getName();
					//拼接键（参数名称.属性名称）
					String realParamName = methodParamName+"."+methodParamFieldName;
					//System.out.println(realParamName);
					//根据参数名查找参数值
					String[] methodParamVals = requestParamVals.get(realParamName);
					if (methodParamVals==null) {
						//没有属性值，跳过
						continue;
					}
					//System.out.println(methodParamVals[0]);
					//获取属性类型
					Class methodParamFieldCl= methodParamFields[j].getType();
					//判断类型是否一致
					/*if (!methodParamFieldCl.isInstance()) {
						continue;
					}*/
					//String转为真实类型
					Object re = parse(methodParamVals, methodParamFieldCl);
					//获取set方法名
					String setMethodFieldName = "set"+methodParamFieldName.substring(0, 1).toUpperCase()
							+methodParamFieldName.substring(1);
					//System.out.println(setMethodFieldName);
					try {
						//获取set方法对象
						Method setMethodField = methodParamTypeCl.getDeclaredMethod(setMethodFieldName, methodParamFieldCl);
						//执行set方法赋值
						setMethodField.invoke(obj, re);
					} catch (Exception e) {
						//如果类型不一致跳过
						continue;
					} 
				}
				//数据存在实参中
				realMethodParam[i] = obj;
			}
		}
		try {
			// 执行当前method
			result = method.invoke(controller,realMethodParam);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	//类型转换方法
	private Object parse(String[] methodParamVals, Class<?> methodParamTypeCl) {
		Object result = null;
		//确认是不是数组
		boolean isArray = false;
		if (methodParamTypeCl.isArray()) {
			//是数组将标志置为true
			isArray = true;
			//获取数组的Class对象，可以通过Array.newInstance反射生成数组对象
			methodParamTypeCl = methodParamTypeCl.getComponentType();
		}
		//判断各种类型
		if (methodParamTypeCl.isAssignableFrom(byte.class)) {
			//当前参数类型是数组
			if (isArray) {
				byte[] re = new byte[methodParamVals.length];
				//循环遍历强转
				for (int i = 0; i < re.length; i++) {
					re[i] = Byte.parseByte(methodParamVals[i]);
				}
				result = re;
			}else{
				result = Byte.parseByte(methodParamVals[0]);
			}
			
		}else if (methodParamTypeCl.isAssignableFrom(short.class)) {
			if (isArray) {
				short[] re = new short[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Short.parseShort(methodParamVals[i]);
				}
				result = re;
			}else{
				result = Short.parseShort(methodParamVals[0]);
			}
		}else if (methodParamTypeCl.isAssignableFrom(int.class)) {
			if (isArray) {
				int[] re = new int[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Integer.parseInt(methodParamVals[i]);
				}
				result = re;
			}else{
				result = Integer.parseInt(methodParamVals[0]);
			}
		}else if (methodParamTypeCl.isAssignableFrom(long.class)) {
			if (isArray) {
				long[] re = new long[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Long.parseLong(methodParamVals[i]);
				}
				result = re;
			}else{
				result = Long.parseLong(methodParamVals[0]);
			}
		}else if (methodParamTypeCl.isAssignableFrom(float.class)) {
			if (isArray) {
				float[] re = new float[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Float.parseFloat(methodParamVals[i]);
				}
				result = re;
			}else{
				result = Float.parseFloat(methodParamVals[0]);
			}
			
		}else if (methodParamTypeCl.isAssignableFrom(double.class)) {
			if (isArray) {
				double[] re = new double[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Double.parseDouble(methodParamVals[i]);
				}
				result = re;
			}else{
			result = Double.parseDouble(methodParamVals[0]);
			}
		}else if (methodParamTypeCl.isAssignableFrom(char.class)) {
			if (isArray) {
				char[] re = new char[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = methodParamVals[i].charAt(i);
				}
				result = re;
			}else{
				result = methodParamVals[0].charAt(0);
			}
		}else if (methodParamTypeCl.isAssignableFrom(boolean.class)) {
			if (isArray) {
				boolean[] re = new boolean[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					re[i] = Boolean.parseBoolean(methodParamVals[i]);
				}
				result = re;
			}else{
			result = Boolean.parseBoolean(methodParamVals[0]);
			}
		}else if (methodParamTypeCl.isAssignableFrom(String.class)) {
			if (isArray) {
				result = methodParamVals;
			}else{
				result = methodParamVals[0];
			}
		}else if (methodParamTypeCl.isAssignableFrom(BigDecimal.class)) {
			if (isArray) {
				BigDecimal[] re = new BigDecimal[methodParamVals.length];
				for (int i = 0; i < re.length; i++) {
					new BigDecimal(methodParamVals[i]);		
				}
				result = re;
			}else{
				result = new BigDecimal(methodParamVals[0]);
			}
		}
		return result;
	}
}
