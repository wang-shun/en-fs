package com.chinacreator.c2.fs.exception;

import java.io.FileNotFoundException;

public class FileNotExsitException extends FileNotFoundException {

	private static final long serialVersionUID = 1373668843453639033L;

	public FileNotExsitException(String id) {
		super("id为[" + id + "]的文件不存在");
	}
}
