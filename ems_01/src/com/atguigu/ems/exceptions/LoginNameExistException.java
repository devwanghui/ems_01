package com.atguigu.ems.exceptions;

public class LoginNameExistException extends RuntimeException{

	private static final long serialVersionUID = 96940249441455507L;

	public LoginNameExistException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoginNameExistException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	public LoginNameExistException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public LoginNameExistException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LoginNameExistException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
	
	
}
