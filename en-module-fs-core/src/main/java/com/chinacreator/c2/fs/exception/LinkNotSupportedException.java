package com.chinacreator.c2.fs.exception;

import com.chinacreator.c2.exception.C2Exception;

/**
 * 当前版本的文件服务器的不支持link方法
 * @author Vurt
 *
 */
public class LinkNotSupportedException extends C2Exception {

	private static final long serialVersionUID = 1717910731129784436L;

	public LinkNotSupportedException(String message) {
		super(message);
	}

}
