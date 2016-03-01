package junit.test;



import java.sql.SQLException;

import javax.sql.DataSource;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.atguigu.ems.entities.Employee;

public class TestEms {
	
	private ApplicationContext atx;
	{
		atx=new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	@Test
	public void test() throws SQLException {
		
		DataSource dataSource = atx.getBean(DataSource.class);
		
		SessionFactory sessionFactory = (SessionFactory) atx.getBean("sessionFactorybean");
		System.out.println(sessionFactory);
		System.out.println(dataSource.getConnection());
		
		Session session = sessionFactory.getCurrentSession();
		
		Criteria criteria = session.createCriteria(Employee.class);
		
		String loginName = "ABCDEFG";
		Criterion criterion = Restrictions.eq("loginName", loginName);
		criteria.add(criterion);
		
		System.out.println((Employee) criteria.uniqueResult());
	}

}
