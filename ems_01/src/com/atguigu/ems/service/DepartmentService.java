package com.atguigu.ems.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atguigu.ems.daos.DepartmentDao;
import com.atguigu.ems.entities.Department;

@Service
public class DepartmentService {
	
	@Autowired
	private DepartmentDao departmnetDao;
	
	@Transactional(readOnly=true)
	public List<Department> getAll(){
		
		return departmnetDao.getAll();
	}
}
