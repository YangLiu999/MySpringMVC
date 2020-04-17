package com.woniuxy.myspringmvc.core;
/**
 * 前端控制器
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woniuxy.myspringmvc.annotation.Controller;
import com.woniuxy.myspringmvc.annotation.RequestMapping;
import com.woniuxy.myspringmvc.annotation.ResponseBody;
import com.woniuxy.myspringmvc.annotation.ResponseForward;
import com.woniuxy.myspringmvc.annotation.ResponseRedirect;


public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	//创建成员变量接收Properties对象
	private Properties application;
	//集合存controller类全名
	private List<String> classNames = new ArrayList<>();
	//创建处理器映射对象
	private HandlerMapper handlerMapper;
	//创建处理器适配器对象
	private HandlerAdapter handlerAdapter;
	
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//System.out.println("service执行");
		//当客户端发起请求时，sevice会根据请求映射找到对应controller对象与Method对象并执行；
		Object result = handlerAdapter.adapter(request, response);
		//处理返回值，生成响应内容：请求转发（@ResponseForword），重定向（@ResponseRedirect）、响应内容
		//判断目标方法是否存在（@ResponseRedirect或者@ResponseForword）
		//非空判断
		if (result==null) return;
		//获取请求映射
		String pathInfo = request.getPathInfo();
		// 查找method对象
		Method method = handlerMapper.getMethodMapping().get(pathInfo);
		//判断是否存在
		if (method.isAnnotationPresent(ResponseForward.class)) {
			//返回值类型转换成String
			String path = (String)result;
			//请求转发
			request.getRequestDispatcher("../../"+path).forward(request, response);
		}else if (method.isAnnotationPresent(ResponseRedirect.class)) {
			//类型转换成String
			String path = (String)result;
			//获取路径方式
			//获取方法上ResponseRedirect注解
			ResponseRedirect responseRedirect = method.getDeclaredAnnotation(ResponseRedirect.class);
			boolean bool = responseRedirect.value();
			if (bool) {
				//绝对路径
				response.sendRedirect(path);
			}else{
				//相对路径
				response.sendRedirect("../../"+path);
			}
			//判断目标方法上是否存在ResponseBody注解
		}else if (method.isAnnotationPresent(ResponseBody.class)) {
			//创建ObjectMapper对象
			ObjectMapper oMapper = new ObjectMapper();
			//设置响应内容类型：在server web.xml下查询
			response.setContentType("application/json;charset = UTF-8");
			//写出到客户端
			oMapper.writeValue(response.getOutputStream(), result);
		}
	}
	
	@Override
	public void init() throws ServletException {
		//System.out.println("*********");
		//加载配置文件获取包名
		String packageName = doLoadConfig();
		//System.out.println(packageName);
		//非空判断
		if (packageName==null) return;
		//扫描包下类的信息，获取类全名
		doScanner(packageName);
		//遍历看对象是否存在controller注解，存在创建对象并按类名小驼峰形式作为键，对象作为值方式存储
		/*doInstance();
		//映射关系建立
		initMapping();*/
		//创建处理器映射器对象
		handlerMapper = new HandlerMapper(classNames);
		//创建处理器适配器对象
		handlerAdapter = new HandlerAdapter(handlerMapper);
	}
	
	//扫描包下所有类文件，获取所有类全名
	private List<String> doScanner(String packageName) {
		//非空判断
		if(packageName==null) return classNames;
		//将包名转为文件路径名
		String path = packageName.replaceAll("\\.", "/");
		//System.out.println(path);
		//类加载器获取文件
		URL url = DispatcherServlet.class.getClassLoader().getResource(path);
		//获取真实路径：web文件中路径和本地文件不一样
		String urlPath = url.getPath();
		//将系统空格转为Java空格
		String realPath = urlPath.replaceAll("%20", " ");
		//创建对象
		File file = new File(realPath);
		//System.out.println(file);
		 //源文件夹或者文件不存在
        if(!file.exists()||!file.isDirectory())
            return classNames;
        //遍历文件夹
        File[] listFiles = file.listFiles();
        //文件夹下面不存在文件
    	if (listFiles == null) return classNames;
    	//循环判断当前文件夹
        for (File file2 : listFiles) {
        	//判断是不是Java文件
        	if(file2.isFile()&&file2.getName().endsWith(".class")){
        		//获取文件名
            	String fileName = file2.getName();
            	//System.out.println(fileName);
            	//获取类名
            	String className = fileName.replace(".class", "");
            	//System.out.println(className);
            	//文件名:类全名
            	String classAllName = packageName+"."+className;
            	//System.out.println(classAllName);
            	//存储类名
            	classNames.add(classAllName);    		}
        	//如果是文件夹，就递归
            if(file2.isDirectory()){
            	//传入当前文件夹的路径
            	doScanner(packageName+"."+file2.getName());
            }
        }
        return classNames;
	}
	
	//查找配置文件获取包名方法
	private String doLoadConfig() {
		// 获取当前servlet config对象
		ServletConfig config = getServletConfig();
		// 获取出初始化参数信息
		String configName = config.getInitParameter("config");
		// 未获取到文件名，设置默认的文件名
		if (configName == null) {
			configName = "application.properties";
		}
		//System.out.println(configName);
		// 新建Properties对象
		application = new Properties();
		// 加载Properties文件:获取类加载器
		InputStream is = DispatcherServlet.class.getClassLoader().getResourceAsStream(configName);
		try {
			//将配置文件加载到application对象中
			application.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			//资源关闭
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 根据键获取Properties文件配置包名
		String packageName = application.getProperty("package");
		// System.out.println(packageName);
		return packageName;
	}
	
}
