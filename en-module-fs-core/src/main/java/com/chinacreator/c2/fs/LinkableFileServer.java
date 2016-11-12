package com.chinacreator.c2.fs;

import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.exception.LinkNotSupportedException;

public interface LinkableFileServer extends FileServer{
	/**
	 * 判断服务器中是否已存在内容相同的文件,并返回
	 * 
	 * @param digest
	 *            文件内容特征码，特征码算法参见{@link ContentDigester}
	 * @return FileMetadata
	 */
	public FileMetadata contains(String digest);

	/**
	 * 假装上传了一个新文件，实际是链接一个服务器中已有的文件，并新建一条新元数据。
	 * 
	 * @param digest
	 *            文件内容特征码，特征码算法参见{@link ContentDigester}
	 * @param metadata
	 *            新的元数据
	 * @return 新文件的定位字符串(PATH字符串，不同服务器实现可能会有不同的形式)
	 * @throws LinkNotSupportedException 文件服务器这个版本可能不支持link动作
	 * @throws FileNotExsitException
	 *             如果不存在指定特征码的文件
	 */
	public String link(FileMetadata metadata) throws LinkNotSupportedException,FileNotExsitException;
	
}
