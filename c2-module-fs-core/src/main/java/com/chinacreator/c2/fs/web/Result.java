package com.chinacreator.c2.fs.web;


/**
 * 文件上传下载返回结果集
 * @author hushowly
 *
 */
public class Result {
	
	/**
	 * 文件上传结果状态
	 */
	private int status;
	
	/**
	 * 文件上传结果信息
	 */
	private String msg;
	
	public Result(int status,String msg){
		this.status=status;
		this.msg=msg;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
