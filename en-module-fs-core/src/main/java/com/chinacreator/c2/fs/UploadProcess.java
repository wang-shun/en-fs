package com.chinacreator.c2.fs;


/**
 * 文件上传处理接口
 * @author hushowly
 * 
 */
public abstract class UploadProcess implements Process{
	
	private String processName;   //处理器名，所有子类需要提供
	
	/**
	 * 构造函数初始化处理名
	 * @param processName
	 */
	public UploadProcess(String processName){
		this.processName=processName;
	}
	
	public String getProcessName(){
		return this.processName;
	}
}