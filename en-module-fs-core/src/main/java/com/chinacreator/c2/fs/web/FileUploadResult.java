package com.chinacreator.c2.fs.web;


/**
 * 单文件上传返回结果集
 * @author hushowly
 *
 */
public class FileUploadResult extends Result{
	
	private String name;
	/**
	 * 文件服务定位文件的字符串
	 */
	private String path;
	
	/**
	 * 文件上传完后的访问路径
	 */
	private String url;
	
	private Long filesize;
	private String mimetype;
	
	
	public FileUploadResult(int status,String msg,String name,String path,String url){
		super(status,msg);
		this.name=name;
		this.path=path;
		this.url=url;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public Long getFilesize() {
		return filesize;
	}


	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}


	public String getMimetype() {
		return mimetype;
	}


	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}
	
	
}
