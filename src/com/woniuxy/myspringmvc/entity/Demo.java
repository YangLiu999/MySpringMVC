package com.woniuxy.myspringmvc.entity;

import java.io.Serializable;
import java.math.BigDecimal;

public class Demo implements Serializable{
	private int id;
	private String name;
	private BigDecimal balance;
	private char sex;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public BigDecimal getBalance() {
		return balance;
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
	public char getSex() {
		return sex;
	}
	public void setSex(char sex) {
		this.sex = sex;
	}
	@Override
	public String toString() {
		return "Demo [id=" + id + ", name=" + name + ", balance=" + balance + ", sex=" + sex + "]";
	}
}
