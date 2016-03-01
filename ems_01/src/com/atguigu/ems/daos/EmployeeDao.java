package com.atguigu.ems.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.sql.JoinType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.atguigu.ems.entities.Employee;
import com.atguigu.ems.orm.Page;

@Repository
public class EmployeeDao {
	@Autowired
	private SessionFactory sessionFactory;
	
	
	public Session getSession(){
		return this.sessionFactory.getCurrentSession();
	}
	
	//编辑用
	public void save(Employee employee) {
		
		getSession().saveOrUpdate(employee);
	}
	
	public void evict(Employee employee){
		getSession().evict(employee);
	}
	
	//删除用
	public Employee get(Integer id){
		return (Employee) getSession().get(Employee.class, id);
	}
	
	public Page<Employee> getPage(Page<Employee> page){
		
		//1.查询totalElement，
		long totalElements = getTotalElements();
		page.setTotalElements(totalElements);
		
		//2.查询content；
		List<Employee> content = getContent(page);
		page.setContent(content);
		
		return page;
	}
	
	//具体查询
	private List<Employee> getContent(Page<Employee> page) {
		
		Criteria criteria = getSession().createCriteria(Employee.class)
										.createAlias("department", "d", JoinType.LEFT_OUTER_JOIN)
										.createAlias("roles", "r", JoinType.LEFT_OUTER_JOIN);
		//起始页数
		int firstResult = (page.getPageNo()-1) * page.getPageSize();
		//每页显示条数
		int maxResults = page.getPageSize();
		
		criteria.setMaxResults(maxResults);
		criteria.setFirstResult(firstResult);
		
		return criteria.list();
	}
	//具体查询
	private long getTotalElements() {
		
		Criteria criteria = getSession().createCriteria(Employee.class);
		
		criteria.setProjection(Projections.count("employeeId"));
		return (long) criteria.uniqueResult();
	}
	
	public Employee getBy(String propertyName, Object propertyValue){
		Criteria criteria = getSession().createCriteria(Employee.class);
		Criterion criterion = Restrictions.eq(propertyName, propertyValue);
		criteria.add(criterion);
		
		return (Employee) criteria.uniqueResult();
	}

	public Employee getByLoginName(String loginName){
		Criteria criteria = getSession().createCriteria(Employee.class);
		Criterion criterion = Restrictions.eq("loginName", loginName);
		criteria.add(criterion);
		
		
		return (Employee) criteria.uniqueResult();
		
	}

	public List<Employee> getAllEmp() {
		Criteria criteria = getSession().createCriteria(Employee.class)
				.createAlias("department", "d", JoinType.LEFT_OUTER_JOIN)
				.createAlias("roles", "r", JoinType.LEFT_OUTER_JOIN);
		return criteria.list();
	}

	public void batchSave(List<Employee> employees) {
		
		for(int i = 0; i< employees.size(); i++){
			getSession().save(employees.get(i));
			
			if((i+1)%50 == 0){
				
				getSession().flush();
				getSession().clear();
			}
		}
	}

	

	
}
