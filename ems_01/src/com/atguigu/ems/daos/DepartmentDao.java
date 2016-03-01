package com.atguigu.ems.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.atguigu.ems.entities.Department;

@Repository
public class DepartmentDao {
	@Autowired
	private SessionFactory sessionFactory;
	
	public Session getSession(){
		return this.sessionFactory.getCurrentSession();
	}
	
	public List<Department> getAll(){
		Criteria criteria = getSession().createCriteria(Department.class);
		criteria.setCacheable(true);
		return criteria.list();
	}
	
	public Department getBy(String propertyName, Object propertyVlaue){
		Criteria criteria = getSession().createCriteria(Department.class);
		Criterion criterion = Restrictions.eq(propertyName, propertyVlaue);
		criteria.add(criterion);
		
		return (Department) criteria.uniqueResult();
	}
}
