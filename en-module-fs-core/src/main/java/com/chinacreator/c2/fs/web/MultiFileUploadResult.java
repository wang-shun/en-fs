package com.chinacreator.c2.fs.web;

import java.util.ArrayList;
import java.util.List;


/**
 * 多文件上传结果集
 * @author hushowly
 *
 */
public class MultiFileUploadResult  extends Result{

	
	private List<FileUploadResult> fileList;
	
	public MultiFileUploadResult(int status,String msg){
		super(status,msg);
	}

	public List<FileUploadResult> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileUploadResult> fileList) {
		this.fileList = fileList;
	}
	
	
	public void addFileUploadResult(FileUploadResult fr){
		if(null==this.fileList){
			this.fileList=new ArrayList<FileUploadResult>();
		}
		this.fileList.add(fr);
	}
	
	
}
