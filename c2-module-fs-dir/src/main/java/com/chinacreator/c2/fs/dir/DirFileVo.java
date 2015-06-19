package com.chinacreator.c2.fs.dir;



public class DirFileVo {
	
	/**
	 * 文件名
	 */
	private String fileName;
	
	/**
	 * 文件路径
	 */
	private String filePath;
	
	/**
	 *文件MIMETYPE
	 */
	private String mimetype;

	/**
	 *文件大小
	 */
	private Long filesize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
	
	
	
}
