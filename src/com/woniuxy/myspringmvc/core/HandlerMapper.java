package com.woniuxy.myspringmvc.core;
/**
 * 请求到处理器的映射
 */
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.woniuxy.myspringmvc.annotation.Controller;
import com.woniuxy.myspringmvc.annotation.RequestMapping;

public class HandlerMapper {
	//map存储有controller注解的对象
	private Map<String, Object> ioc = new HashMap<>();
	// map存储请求映射与controller对应关系
	private Map<String, Object> controllerMapping = new HashMap<>();
	// map存储请求映射与Method对应关系
	private Map<String, Method> methodMapping = new HashMap<>();
	
	public HandlerMapper(List<String> classNames){
		//在实例化是直接调用doInstance和initMapping方法
		doInstance(classNames);
		initMapping();
	}
	
	public Map<String, Object> getIoc() {
		return ioc;
	}

	public Map<String, Object> getControllerMapping() {
		return controllerMapping;
	}

	public Map<String, Method> getMethodMapping() {
		return methodMapping;
	}
	//遍历看对象是否存在controller注解，存在则创建对象并按类名小驼峰形式作为键，对象作为值方式存储
	private Map<String, Object> doInstance(List<String> classNames) {
		// 非空判断
		if (classNames == null)
			return ioc;
		// 循环创建类对象
		for (String string : classNames) {
			try {
				// 创建当前类对象
				Class cl = Class.forName(string);
				// 判断是否存在controller注解
				boolean re = cl.isAnnotationPresent(Controller.class);
				if (re) {
					// 存在controller注解
					// 创建当前controller对象
					Object obj = cl.newInstance();
					// 获取类名
					String className = cl.getSimpleName();
					// 类名转化为小驼峰
					String realClassName = className.substring(0, 1).toLowerCase() + className.substring(1);
					// System.out.println(realClassName);
					// 存储到map中
					ioc.put(realClassName, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ioc;
	}
	
	//映射关系建立
	private void initMapping() {
		// 遍历获取Controller对象
		for (Object controller : ioc.values()) {
			// 获取目标类的类对象
			Class<? extends Object> cl = controller.getClass();
			// 判断是否有RequestMapping注解
			boolean re = cl.isAnnotationPresent(RequestMapping.class);
			// 不存在RequestMapping注解直接跳过
			if (!re) continue;
			// 获取RequestMapping注解
			RequestMapping declaredAnno = cl.getDeclaredAnnotation(RequestMapping.class);
			// 获取当前Controller映射值
			String controllerMappingVal = declaredAnno.value();
			// 获取当前Controller方法
			Method[] methods = cl.getDeclaredMethods();
			//循环判断方法是否存在RequestMapping注解
			for (Method method : methods) {
				// 判断当前方法是否存在注解
				if (!method.isAnnotationPresent(RequestMapping.class)) {
					continue;
				}
				// 获取方法注解
				RequestMapping methodRM = method.getDeclaredAnnotation(RequestMapping.class);
				// 获取RequestMapping注解值
				String methodMappingVal = methodRM.value();
				// 判断值
				if (controllerMappingVal.length() < 1 || methodMappingVal.length() < 1) {
					continue;
				}
				// 存储
				String key = controllerMappingVal + methodMappingVal;
				// System.out.println(key);
				// 建立请求映射和Controller映射
				controllerMapping.put(key, controller);
				// 建立请求映射和method映射
				methodMapping.put(key, method);
			}
		}

	}
}
