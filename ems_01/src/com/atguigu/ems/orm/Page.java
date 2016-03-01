package com.atguigu.ems.orm;

import java.util.List;



public class Page<T> {
	private int pageNo = 1;
	private int pageSize = 5;

	private long totalElements;
	private List<T> content;
	
	public boolean isHasPrev(){
		return (this.pageNo > 1);
	}
	
	//5. 有没有后一页
	public boolean isHasNext(){
		return (this.pageNo < getTotalPage());
	}
	
	public int getTotalPage(){
		
		int totalPage = (int) ((totalElements + pageSize - 1)/pageSize);
		
		return totalPage;
	}
	
	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		
		if(pageNo<1){
			pageNo=1;
		}
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public long getTotalElements() {
		return totalElements;
	}
	
	
	//逻辑错误，应该先返回totalElement,在验证pageNo的合法性。
	//因为必须先从totalElement去查询totalPage,再用totalPage去验证pageNo的合法性
	
	public void setTotalElements(long totalElements) {
		
		this.totalElements = totalElements;
		if(this.pageNo> getTotalPage()){
			this.pageNo = getTotalPage();
		}
		
	}
	
	

	public List<T> getContent() {
		return content;
	}

	public void setContent(List<T> content) {
		this.content = content;
	}

}
