package com.atguigu.ems.daos;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.atguigu.ems.entities.Role;
@Repository
public class RoleDao {
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public Session getSession(){
		return sessionFactory.getCurrentSession();
	}
	
	//获取Role的List,
	public List<Role> getAll(){
		
		Criteria criteria  =getSession().createCriteria(Role.class);
		//设置二级缓存，
		criteria.setCacheable(true);
		return criteria.list();
		
	}
	
	//获取Role的集合，
	public Collection<Role> getRoles(String propertyName, String[] propertyVal){
		
		Criteria criteria = getSession().createCriteria(Role.class);
		Criterion criterion = Restrictions.in(propertyName, propertyVal);
		criteria.add(criterion);
		return criteria.list();
		
	}
}
