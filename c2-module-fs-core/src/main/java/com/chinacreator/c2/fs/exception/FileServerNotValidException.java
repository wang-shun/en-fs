package com.chinacreator.c2.fs.exception;

import com.chinacreator.c2.exception.C2RuntimeException;

public class FileServerNotValidException extends C2RuntimeException {

	public FileServerNotValidException() {
		super("文件服务器不可用");
	}

	private static final long serialVersionUID = -5463238988082423861L;

	@Override
	public String getErrorNum() {
		return "800";
	}

}
