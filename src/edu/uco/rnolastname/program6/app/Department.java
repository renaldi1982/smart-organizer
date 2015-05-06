package edu.uco.rnolastname.program6.app;

import java.util.ArrayList;
import java.util.HashMap;

public class Department {
	private String dName;
	private String dPhone;
	private String dWeb;
		
	public Department(){
		
	}
	
	public Department(String name, String phone, String web){
		this.dName = name;
		this.dPhone = phone;
		this.dWeb = web;
	}
	
	public String getDName(){
		return dName; 
	}
	
	public String getDPhone(){
		return dPhone; 
	}
	
	public String getDWeb(){
		return dWeb; 
	}		
}
