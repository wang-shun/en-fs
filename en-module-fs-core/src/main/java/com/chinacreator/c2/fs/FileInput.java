package com.chinacreator.c2.fs;

import java.io.InputStream;

public class FileInput {
	
	private FileMetadata fileMetadata;
	private InputStream inputStream;
	
	public FileMetadata getFileMetadata() {
		return fileMetadata;
	}
	public void setFileMetadata(FileMetadata fileMetadata) {
		this.fileMetadata = fileMetadata;
	}
	public InputStream getInputStream() {
		return inputStream;
	}
	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}
	

	
	
	
}
