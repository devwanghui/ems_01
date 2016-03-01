package com.atguigu.ems.interceptors;

import java.io.File;
import java.util.Map;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

public class FileCleanInterceptor implements Interceptor {

	private static final long serialVersionUID = 1L;

	@Override
	public void destroy() {
		
	}

	@Override
	public void init() {
	}

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		String result = invocation.invoke();
		
		//删除临时文件
		//从request中获取下载文件
		Map<String, Object> request = (Map<String, Object>) invocation.getStack().getContext().get("request");
		String excelFileName = (String) request.get("excelFileName");
		
		if(excelFileName !=null){
			File file  = new File(excelFileName);
			if(file.exists()){
				file.delete();
			}
		}
		
		return result;
	}

	
}
