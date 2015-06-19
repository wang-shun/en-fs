package com.chinacreator.c2.fs.dir;

import java.io.File;
import java.util.LinkedList;

import org.springframework.util.StringUtils;

public class DirFile {
	
	private String fileName;
	
	private LinkedList<String> pathList=new LinkedList<String>();
	
	/**
	 *文件MIMETYPE
	 */
	private String mimetype;

	/**
	 *文件大小
	 */
	private Long filesize;
	
	/**
	 * 真实文件
	 */
	private File file;

	
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public LinkedList<String> getPathList() {
		return pathList;
	}

	public void setPathList(LinkedList<String> pathList) {
		this.pathList = pathList;
	}


	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * 初始化
	 * @param filePath
	 * @throws Exception
	 */
	public DirFile(String filePath) throws Exception{
		
		if(StringUtils.isEmpty(filePath)){
			throw new Exception("文件路径不正确！");
		}
		
		String []dirs=filePath.split("\\\\");
		for (int i=0;i<dirs.length;i++) {
			if(i!=dirs.length-1){
				if(StringUtils.isEmpty(dirs[i])) continue;
				pathList.addLast(dirs[i]);
			}else{
				fileName=dirs[i];
				if(StringUtils.isEmpty(fileName)){
					throw new Exception("文件路径不正确！");
				}
			}
		}
	}
	
	public String getFileName(){
		return this.fileName;
	}
	
	
	/**
	 * 获取路径
	 * @return
	 */
	public String getParentPath(){
		String path="";
		for (String str : pathList) {
			path+=File.separator+str;
		}
		
		if(StringUtils.isEmpty(path)){
			path="/";
		}
		return path;
	}
	
	
	/**
	 * 获取路径
	 * @return
	 */
	public String getFullPath(){
		String path="";
		for (String str : pathList) {
			path+=File.separator+str;
		}
		
		if(StringUtils.isEmpty(path)){
			path="/";
		}
		return path+File.separator+this.fileName;
	}
}
