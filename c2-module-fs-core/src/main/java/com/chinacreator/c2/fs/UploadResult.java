package com.chinacreator.c2.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chinacreator.c2.fs.web.FileUploadResult;

public class UploadResult {
	private int status;
	private String msg;
	private List<FileUploadResult> fileList;
	private Map<String,Object> paramsMap;
	

	public void addFileUploadResult(FileUploadResult fm){
		if(null==fileList){
			fileList=new ArrayList<FileUploadResult>();
		}
		this.fileList.add(fm);
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



	public List<FileUploadResult> getFileList() {
		return fileList;
	}

	public void setFileList(List<FileUploadResult> fileList) {
		this.fileList = fileList;
	}

	public Map<String, Object> getParamsMap() {
		return paramsMap;
	}

	public void setParamsMap(Map<String, Object> paramsMap) {
		this.paramsMap = paramsMap;
	}


	
	
	
}
