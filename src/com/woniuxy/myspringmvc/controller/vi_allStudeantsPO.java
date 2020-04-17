package com.woniuxy.myspringmvc.controller;
/**
 * 视图->PO->DAO->service->servlet
 * @author Administrator
 *
 */
public final class vi_allStudeantsPO {
	private int sid;
	private String sname;
	private String sex;
	private int age;
	private String tel;
	private int class_id;
	private String cname;
	
	public vi_allStudeantsPO()  {
		super();
	}
	public vi_allStudeantsPO(int sid, String sname, String sex, int age, String tel, int class_id, String cname) {
		super();
		this.sid = sid;
		this.sname = sname;
		this.sex = sex;
		this.age = age;
		this.tel = tel;
		this.class_id = class_id;
		this.cname = cname;
	}
	public int getSid() {
		return sid;
	}
	public void setSid(int sid) {
		this.sid = sid;
	}
	public String getSname() {
		return sname;
	}
	public void setSname(String sname) {
		this.sname = sname;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getTel() {
		return tel;
	}
	public void setTel(String tel) {
		this.tel = tel;
	}
	public int getClass_id() {
		return class_id;
	}
	public void setClass_id(int class_id) {
		this.class_id = class_id;
	}
	public String getCname() {
		return cname;
	}
	public void setCname(String cname) {
		this.cname = cname;
	}
	@Override
	public String toString() {
		return "vi_allStudeantsPO [sid=" + sid + ", sname=" + sname + ", sex=" + sex + ", age=" + age + ", tel=" + tel
				+ ", class_id=" + class_id + ", cname=" + cname + "]";
	}
	
}
