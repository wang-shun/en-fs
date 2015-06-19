package com.chinacreator.c2.fs;

import java.io.InputStream;

import com.chinacreator.c2.fs.web.Result;

public class DownResult{
	private InputStream inputStream;
	private FileMetadata fileMetadata;
	
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	public FileMetadata getFileMetadata() {
		return fileMetadata;
	}
	public void setFileMetadata(FileMetadata fileMetadata) {
		this.fileMetadata = fileMetadata;
	} 
	
	
}
