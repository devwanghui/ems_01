package com.atguigu.ems.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atguigu.ems.daos.EmployeeDao;
import com.atguigu.ems.entities.Employee;
import com.atguigu.ems.exceptions.LoginNameNotFoundException;

@Service
public class EmployeeService {
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Transactional
	public Employee login(String loginName, String password){
		
		Employee employee = employeeDao.getByLoginName(loginName);
		
	
		
		return employee;
	}
}
