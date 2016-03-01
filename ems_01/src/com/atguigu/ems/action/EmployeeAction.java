package com.atguigu.ems.action;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.RequestAware;
import org.apache.struts2.interceptor.SessionAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.atguigu.ems.entities.Employee;
import com.atguigu.ems.exceptions.LoginNameExistException;
import com.atguigu.ems.exceptions.LoginNameNotFoundException;
import com.atguigu.ems.exceptions.LoginNameNotMatchPasswordExcrption;
import com.atguigu.ems.orm.Page;
import com.atguigu.ems.service.DepartmentService;
import com.atguigu.ems.service.EmployeeService;
import com.atguigu.ems.service.RoleService;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;
import com.opensymphony.xwork2.Preparable;

@Scope("prototype")
@Controller
public class EmployeeAction extends ActionSupport implements RequestAware,
		SessionAware, Preparable, ModelDriven<Employee>, ParameterAware {

	private static final long serialVersionUID = 1L;

	private Map<String, Object> requestMap;

	private Map<String, Object> sessionMap;
	
	private Map<String, String[]> paramsMap;
	
	private Integer id;
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return id;
	}
	
	private Employee model;
	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private DepartmentService departmentService;
	@Autowired
	private RoleService roleService;
	
	private Page<Employee> page = new Page<>();
	
	public void setPage(Page<Employee> page) {
		this.page = page;
	}
	
	public Page<Employee> getPage() {
		return page;
	}
	
	public void prepareSave(){
		if(id == null)
			model = new Employee();
		else{
			model = employeeService.get(id);
			model.getRoles().clear();
			model.setDepartment(null);
		}
	}
	
	
	public String list3(){
		
		return "emp-criteriaInput";
	}
	
	
	public String save(){
		String oldLoginName = "";
		
		try {
			String [] vals = paramsMap.get("oldLoginName");
			if(vals != null && vals.length == 1){
				oldLoginName = vals[0];
			}
			
			employeeService.save(model, oldLoginName);
		} catch (LoginNameExistException e) {
			addFieldError("loginName", getText("error.employee.save.loginName", Arrays.asList(model.getLoginName()))); 
			return "emp-save-input";
		}
		
		return "emp-success";
	}
	
	//下载相关属性
	private String contentType;
	private long contentLength;
	private String fileName;
	private InputStream inputStream;
	
	//下载get方法
	public long getContentLength() {
		return contentLength;
	}
	
	public String getContentType() {
		return contentType;
	}
	
	public String getContentDisposition() {
		return "attachment;filename=" + fileName;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	//下载
	public String downToExcel() throws IOException{
		//初始化属性
		
		contentType = "application/vnd.ms-excel";
		
		String excelFileName = ServletActionContext.getServletContext().getRealPath("/files/"+System.currentTimeMillis()+".xls");
		employeeService.downLoad(excelFileName);
		System.out.println(excelFileName);
		requestMap.put("excelFileName", excelFileName);
		
		inputStream = new FileInputStream(excelFileName);
		contentLength = inputStream.available();
		fileName = "employee.xls";
		
		/*静态
		 * contentType = "application/vnd.ms-excel";
		
		inputStream = new FileInputStream("d://workbook.xls");
		contentLength = inputStream.available();
		fileName = "employee.xls";*/
		return "excel-result";
	}
	//上传属性
	private File file;
	
	public void setFile(File file) {
		this.file = file;
	}
	//上传
	public String upload() throws InvalidFormatException, IOException{
		//解析文件
		//当文件上传失败时，显示Excel中的详细错误的集合
		List<String[]> errors = employeeService.upload(file);
		//如果有错，则把用errorMessage接收（String型的数组的集合内容为错误的行号和列号）错误消息（是为填充i18n中error.employee.upload对应的value中的占位符）
		if(errors !=null && errors.size()>0){
			StringBuilder errorMessages = new StringBuilder();
			//遍历获得的错误消息集合，
			for(String[] error :errors){
				//getText方法是将填充利用error的String[]中的行号和列号，将error.employee.upload对应的value填充后返回String型的value值
				String errorMessage = getText("error.employee.upload", error);
				errorMessages.append(errorMessage);
				
			}
			//添加错误消息
			addActionError(errorMessages.toString());
			return "input";
		}
		return "upload-success";
	}
	
	//下载模板
	public String uploadTemplateDownload() throws IOException{
		
		contentType = "application/vnd.ms-excel";
		
		String excelFileName = ServletActionContext.getServletContext().getRealPath("/files/employee.xls");
		
		inputStream = new FileInputStream(excelFileName);
		contentLength = inputStream.available();
		fileName = "employee.xls";
		
		return "excel-result";
	}
	
	public void prepareInput(){
		if(id!=null){
			this.model = employeeService.get(id);
		}
	}
	//修改页面
	public String input(){
		System.out.println(".........................妈蛋");
		this.requestMap.put("roles",roleService.getAll());
		
		System.out.println("--------------------------haha");
		this.requestMap.put("departments", departmentService.getAll());
		
		
		return "emp-input";
	}
	
	//Ajax直接返回标记位，所以该方法没有返回值，直接把结果print
	public void delete() throws IOException{
		//直接返回标记位，
		String result = "0";
		result = employeeService.delete(id)+"";
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.getWriter().print(result);
	}
	
	//使用struts2返回Json对象；
	//1.加入jar包， struts2-json-plugin-2.3.15.3.jar
	//2.使用Json结果类型
	//3.可以通过root设置属性，使struts2只返回Action root对应的Json数据
	//此处有两个需要注意的点，其一，在struts.xml配置时，
	//需要设置<package name="default" namespace="/" extends="json-default">
	//另外在获取content集合时，需设置<param name="excludeProperties">，来剔除不需要的元素
	//剔除原因，获取关联属性时，可能在关联睡醒内部会有其他关联到当前类的元素，会产生异常。
	//需要用类似content.*\.roles此种形式来剔除
	//4.还有content中的departmentName，获取麻烦，可以在employee类中，定义一个工具方法。
	public String list2(){
		
		this.page = employeeService.getPage(page);
		return "list2";
	}
	
	public String list(){
		
		this.page = employeeService.getPage(page);
		
		return "list";
	}

	
	
	//Ajax验证，并未经过struts2
	public void validateLoginName() throws IOException{
		
		String result = "2";
		String loginName = paramsMap.get("loginName")[0];
		try {
			employeeService.login(loginName, "");
		} catch ( LoginNameNotFoundException e) {
			result="1";
		} catch (LoginNameNotMatchPasswordExcrption e){
			result = "0";
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.getWriter().print(result);
	}
	
	
	public void prepareLogin() {
		model = new Employee();
		
	}
	
	public String login() {
		
		String loginName = model.getLoginName();
		String password = model.getPassword();
		
		Employee employee = employeeService.login(loginName, password);
		sessionMap.put("employee", employee);
		return "success";
	}

	@Override
	public Employee getModel() {

		return model;
	}

	@Override
	public void prepare() throws Exception {

	}

	@Override
	public void setSession(Map<String, Object> arg0) {
		this.sessionMap = arg0;
	}

	@Override
	public void setRequest(Map<String, Object> arg0) {
		this.requestMap = arg0;
	}

	@Override
	public void setParameters(Map<String, String[]> arg0) {
		this.paramsMap = arg0;
		
	}

}
