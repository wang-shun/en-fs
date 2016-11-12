package com.chinacreator.c2.fs;

import java.io.InputStream;

import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.util.ContentTypeUtil;

/**
 * 文件存储服务统一接口，负责读写文件，还提供接口来维护文件的元数据{@link FileMetadata}。元数据源是一个实体JavaBean，可以由文件存储服务自行决定是否序列化，怎么序列化。
 * 
 * <p>文件存储服务还约定了文件以一个全局唯一的识别字符串来定位，文件服务不限定该字符串的形式，可以是相对路径，也可以是任意其他自己生成的id之类的(比如每个文件分配一个uuid，
 * 只要根据该字符串可以读取到文件内容即可</p>
 */
public interface FileServer {
	/**
	 * 文件服务当前是否有效
	 * 
	 * @return
	 */
	public boolean isValid() throws Exception;

	/**
	 * 文件系统中是否存在文件
	 * 
	 * @param path
	 *            文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @return 是否存在
	 */
	public boolean exsits(String path) throws Exception;

	/**
	 * <p>添加文件，如果输入的元数据中没设置mimeType值，那么服务需要根据元数据中的文件名和流的内容自动进行解析</p>
	 * <p>通过流解析文件内容类型可以使用 {@link ContentTypeUtil#getContentType(java.io.BufferedInputStream, String)}方法实现；</p>
	 * 添加完成后不会关闭流，由调用者自己关闭。
	 * 
	 * @param content
	 *            文件内容
	 * @param metadata
	 *            文件元数据(包括源文件名，内容类型等)
	 * @return 文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 */
	public FileMetadata add(InputStream content, FileMetadata metadata) throws Exception;

	/**
	 * 更新文件内容，如果文件不存在，或文件是只读状态等原因都会导致文件无法更新；
	 * 实际上就是删除原始文件再重新建一个，只是id不变，其实文件已经不是老的那份了
	 * 
	 * @param path
	 *            文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @param content
	 *            新内容
	 * @return 是否更新成功
	 * @throws FileNotExsitException
	 *             指定文件不存在
	 */
	public boolean update(String path, InputStream content) throws Exception;

	/**
	 * 删除文件
	 * 
	 * @param path
	 *            文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @param force
	 *            是否强制删除，true:如果文件当前是不可删除状态，那么线程一直阻塞，直到文件成功删除；false:
	 *            如果文件当前是可不可删除状态则直接返回删除不成功
	 * @return 是否删除成功
	 */
	public boolean delete(String path, boolean force) throws Exception;

	/**
	 * 读取文件，如果文件当前是不可读状态(正在被修改)，则会阻塞线程直到文件可读
	 * 
	 * @param path
	 *            文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @return 文件内容
	 * @throws FileNotExsitException
	 *             指定文件不存在
	 */
	public InputStream get(String path) throws Exception;

	/**
	 * 获取文件元数据
	 * 
	 * @param path
	 *            文件的定位字符串(PATH字符串，不同服务实现可能会有不同的形式)
	 * @return 文件元数据
	 * @throws FileNotExsitException 文件不存在
	 */
	public FileMetadata getMetaData(String path) throws Exception;
}
