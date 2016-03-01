package com.atguigu.ems.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atguigu.ems.daos.DepartmentDao;
import com.atguigu.ems.daos.EmployeeDao;
import com.atguigu.ems.daos.RoleDao;
import com.atguigu.ems.entities.Department;
import com.atguigu.ems.entities.Employee;
import com.atguigu.ems.entities.Role;
import com.atguigu.ems.exceptions.EmployeeUnableException;
import com.atguigu.ems.exceptions.LoginNameExistException;
import com.atguigu.ems.exceptions.LoginNameNotFoundException;
import com.atguigu.ems.exceptions.LoginNameNotMatchPasswordExcrption;
import com.atguigu.ems.orm.Page;


@Service
public class EmployeeService {
	@Autowired
	private EmployeeDao employeeDao;
	@Autowired
	private DepartmentDao departmentDao;
	
	@Autowired
	private RoleDao roleDao;
	
	@Transactional
	public void save(Employee employee, String oldLoginName){
		// save
				if (employee.getEmployeeId() == null) {
					employee.setPassword("123456");
					employee.setVisitedTimes(0);
				}

				// update
				if (employee.getEmployeeId() == null
						|| !employee.getLoginName().equals(oldLoginName)) {
					if (employee.getEmployeeId() != null) {
						employeeDao.evict(employee);
					}

					Employee employee2 = employeeDao.getBy("loginName", employee.getLoginName());
					if (employee2 != null) {
						throw new LoginNameExistException();
					}
				}

				employeeDao.save(employee);
			}
	@Transactional
	public Employee getBy(String loginName){
		
		return employeeDao.getByLoginName(loginName);
	}

	@Transactional(readOnly = true)
	public Page<Employee> getPage(Page<Employee> page) {

		return employeeDao.getPage(page);
	}
	
	@Transactional
	public List<Employee> getAll(){
		return employeeDao.getAllEmp();
	}

	@Transactional
	public int delete(Integer id) {
		// 1.判断id对应的emp是不是dept的一个manager，
		Employee manager = new Employee();
		manager.setEmployeeId(id);
		Department department = departmentDao.getBy("manager", manager);
		if (department != null) {
			return 1;
		}

		// 2.若不是manager，则把isDeleted字段修改为1；
		Employee employee = employeeDao.get(id);
		employee.setIsDeleted(1);

		return 2;
	}

	@Transactional
	public Employee login(String loginName, String password) {

		Employee employee = employeeDao.getByLoginName(loginName);

		if (employee == null) {
			throw new LoginNameNotFoundException("用户名" + loginName + "不存在!");
		}
		if (!employee.getPassword().equals(password)) {
			throw new LoginNameNotMatchPasswordExcrption("用户名" + loginName
					+ "和密码不匹配");
		}
		if (employee.getEnabled() != 1) {
			throw new EmployeeUnableException("用户" + loginName + "已被锁定");
		}
		employee.setVisitedTimes(employee.getVisitedTimes() + 1);

		return employee;
	}

	// 下载方法
	@Transactional(readOnly = true)
	public void downLoad(String excelFileName) throws IOException {
		// 创建excel
		Workbook wb = new XSSFWorkbook(); // or new HSSFWorkbook();
		// 创建工作区
		Sheet sheet = wb.createSheet("employees");

		// 1.标题行
		createTitle(sheet);

		// 2.填写内容
		createEmplpyeeRows(sheet);

		// 3.调整样式
		setCellStyle(wb);
		// 4.写到磁盘

		FileOutputStream fileOut = new FileOutputStream(excelFileName);
		wb.write(fileOut);
		fileOut.close();

	}
	
	private void setCellStyle(Workbook wb) {
		Sheet sheet = wb.getSheet("employees");
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			// 创建行
			Row row = sheet.getRow(i);
			// 设置行高
			row.setHeightInPoints(30);

			// 在行循环内，循环列，即，给每个单元格，设置风格
			for (int j = 0; j < TITLES.length; j++) {
				Cell cell = row.getCell(j);
				// 此处getCellStyle为方法，是设置风格的
				cell.setCellStyle(getCellStyle(wb));
			}
		}

		for (int j = 0; j < TITLES.length; j++) {
			// 设置列
			sheet.autoSizeColumn(j);
			// 设置列宽
			sheet.setColumnWidth(j, (int) (sheet.getColumnWidth(j) * 1.4));
		}
	}

	// 设置风格的方法，返回
	
	private CellStyle getCellStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLUE.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());

		return style;
	}

	// 创建后面的数据
	
	private void createEmplpyeeRows(Sheet sheet) {
		// 获取数据
		List<Employee> empList = employeeDao.getAllEmp();
		// 循环添加表内数据，i为行数，即employee条数
		for (int i = 0; i < empList.size(); i++) {
			Employee emp = empList.get(i);
			Row row = sheet.createRow(i + 1);

			Cell cell = row.createCell(0);
			cell.setCellValue("" + (i + 1));

			cell = row.createCell(1);
			cell.setCellValue(emp.getLoginName());

			cell = row.createCell(2);
			cell.setCellValue(emp.getEmployeeName());
			
			cell = row.createCell(3);
			cell.setCellValue(emp.getGender());
			
			cell = row.createCell(4);
			cell.setCellValue(emp.getEnabled());

			cell = row.createCell(5);
			cell.setCellValue(emp.getDepartment().getDepartmentName());

			cell = row.createCell(6);
			cell.setCellValue(emp.getEmail());

			cell = row.createCell(7);
			cell.setCellValue(emp.getRoleNames());

		}
	}

	// 创建首行
	
	private static final String[] TITLES = new String[] { "序号", "登录名", "姓名","性别",
			"登录许可", "部门", "Email", "角色" };
	
	private void createTitle(Sheet sheet) {
		// 创建第一行，索引为初始为0；
		Row row = sheet.createRow(0);

		// 将String数组中的数据传入传入单元格
		for (int i = 0; i < TITLES.length; i++) {
			row.createCell(i).setCellValue(TITLES[i]);
		}
	}
	
	//上传方法readOnly表示什么意思？什么时候加，

	@Transactional
	public List<String[]> upload(File file) throws InvalidFormatException, IOException {
		//读取file文件
		InputStream inp = new FileInputStream(file);
	    Workbook wb = WorkbookFactory.create(inp);
	    //获取sheet对象
	    Sheet sheet = wb.getSheet("employees");		
	    //3.解析文件
	    
	    List<Employee> employees = new ArrayList<>();
	    
	    //获取错误集合的方法，参数为上传Excel的sheet工作表，其中有employee信息，employees集合，为存放解析后的信息。
	    List<String[]> errors = parseToEmplpoyeeList(sheet, employees);
	    
	    //判断errors，存在则返回，不存在，则继续
	    if(errors!= null && errors.size() > 0){
	    	return errors;
	    }
	    
	    //4.批量插入数据到数据库
	    employeeDao.batchSave(employees);
	    
	    //5.返回错误消息 
		return null;
	}
	
	//
	
	private List<String[]> parseToEmplpoyeeList(Sheet sheet,
			List<Employee> employees) {
		
		List<String[]> errors = new ArrayList<>();
		
		//此处i为1，因为第一行为TITLES，标题
		for(int i = 1; i<= sheet.getLastRowNum(); i++){			
			Row row = sheet.getRow(i);
			
			//为什么i+1, 因为行号以1起始，而解析时则从0行开始,解析行（i）中的每一个单元格，并验证是否符合条件，符合则返回employee；
			//
			Employee employee = parseToEmployee(row, i+1, errors);
			
			if(employee!= null){
				
				employees.add(employee);
			}
			
		}

		return errors;
	}
	
	private Employee parseToEmployee(Row row, int i, List<String[]> errors) {
		
		//1.先将employee置空
		Employee employee = null;
		//验证，通过则直接返回employee，不通过，则返回，添加错误消息。
		// 登录名 姓名   性别 登录许可 部门 Email 角色
		
		//1.验证登录名
		Cell cell = row.getCell(1);
		String loginName = getCellValue(cell);
		
		boolean flag  = validateLoginName(loginName);
		
		if(!flag){
			errors.add(new String[]{ i+"", "2"});
		}
		//2.姓名不用验证
		cell = row.getCell(2);
		String employeeName = getCellValue(cell);
		
		//3.验证登录名
		cell  = row.getCell(3);
		String gender = getCellValue(cell);
		gender = validateGender(gender);
		
		if(gender ==null){
			errors.add(new String[]{i+"", "4"});
			flag = false;
		}
		
		//4.验证登陆许可
		cell = row.getCell(4);
		String enabledStr = getCellValue(cell);
		int enabled = validateEnabled(enabledStr);
		
		if(enabled == -1){
			errors.add(new String[]{i+"", "5"});
			flag = false;
		}
		
		//5.验证部门
		cell = row.getCell(5);
		String departmentName = getCellValue(cell);
		Department department = validateDepartmentName(departmentName);
		
		if(departmentName == null){
			errors.add(new String[]{i+"", "6"});
			flag = false;
		}
		
		//6.验证Email；验证时可以返回一个boolean值，验证时，验证是否符合正则表达式。
		cell = row.getCell(6);
		String email = getCellValue(cell);

		if(!validateEmail(email)){
			errors.add(new String[]{i+"", "7"});
			flag = false;
		}
		
		//7.验证角色
		cell = row.getCell(7);
		String roleNames = getCellValue(cell);
		Set<Role> roles = validateRoles(roleNames);
		
		//split(",")将字符串以逗号隔开，返回一个字符串数组；
		if(roles ==null || roles.size() == 0 || roles.size() !=roleNames.split(",").length){
			errors.add(new String[]{i+"", "8"});
			flag = false;
		}
		
		if (flag) {
			employee = new Employee(loginName, employeeName, roles, enabled,
					department, gender, email);
		}
		
		return employee;
	}
	
	//========================y验证方法区============================
	
	private Set<Role> validateRoles(String roleNames) {
		
		Collection<Role> roles = roleDao.getRoles("roleName", roleNames.split(","));
		return new HashSet<>(roles);
	}
	
	private boolean validateEmail(String email) {
		
		Pattern p = Pattern.compile("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	private Department validateDepartmentName(String departmentName) {
		
		return departmentDao.getBy("departmentName", departmentName);
	}
	
	private int validateEnabled(String enabledStr) {
		try {
			double d = Double.parseDouble(enabledStr);
			if(d ==1.0d || d ==0.0d){
				return (int)d;
			}
		} catch (NumberFormatException e) {}
		return -1;
	}
	
	private String validateGender(String gender) {
		
		try {
			double d = Double.parseDouble(gender);
			if(d == 1.0d || d == 0.0d){
				return (int)d+"";
			}
		} catch (NumberFormatException e) {}
		return null;
	}
	
	private boolean validateLoginName(String loginName) {
		if(loginName != null 
				&& !loginName.trim().equals("")
				&&loginName.trim().length() >= 6){
			
			//正则验证
			Pattern p = Pattern.compile("^[a-zA-Z]\\w+\\w$");
			Matcher m = p.matcher(loginName.trim());
			boolean b = m.matches();
			
			//验证loginName是否在数据库中；
			if(b){
				Employee employee = employeeDao.getByLoginName(loginName.trim());
				if(employee == null){
					return true;
				}
				
			}
		}
		return false;
	}

	//创建解析每一个单元格的数据的方法
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private String getCellValue(Cell cell) {
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().getString();
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				Date date = cell.getDateCellValue();
				return dateFormat.format(date);
			} else {
				return cell.getNumericCellValue() + "";
			}
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue() + "";
		case Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula() + "";
		}

		return null;
	}
	@Transactional
	public Employee get(Integer id) {
		Employee employee = employeeDao.get(id);
		return employee;
	}


}
