package com.chinacreator.c2.fs.exception;

import com.chinacreator.c2.exception.C2RuntimeException;

/**
 * User: Vurt
 * Date: 13-8-30
 * Time: 上午10:15
 */
public class InvalidFilePathException  extends C2RuntimeException{
	private static final long serialVersionUID = 8668992913162559884L;

	public InvalidFilePathException(String message) {
        super(message);
    }
}
