package com.chinacreator.c2.fs.inner;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chinacreator.c2.dao.Dao;
import com.chinacreator.c2.dao.DaoFactory;
import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.FileServer;
import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.LinkableFileServer;
import com.chinacreator.c2.fs.exception.FileNotExsitException;
import com.chinacreator.c2.fs.util.ContentTypeUtil;
import com.chinacreator.c2.id.IDGenerator;

/**
 * 本地简单文件服务器实现
 * 
 * @author Vurt
 */
public class SimpleFileServer implements LinkableFileServer,HTTPFileServer{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(FileServer.class);
	private File root;

	/**
	 * 创建简单的文件服务器
	 * 
	 * @param rootPath
	 *            文件服务器根目录
	 */
	public SimpleFileServer(String rootPath) {
		this.root = new File(rootPath);
		if (this.root.exists() && this.root.isDirectory()) {
			valid = true;
		} else {
			boolean succes = this.root.mkdirs();
			if (!succes) {
				LOGGER.error("本地简单文件服务器初始化失败，根目录" + root.getAbsolutePath()
						+ "不存在且无法被创建，" + "可能是存在同名文件，或没有权限创建目录");
			}
		}
		try {
			valid = true;
			LOGGER.info("简单文件服务器初始化成功，储存根目录：{}", root.getAbsolutePath());
		} catch (Exception e) {
			LOGGER.error("初始化文件服务器元数据管理器时发生错误:{}，简单文件服务器无效!!!", e.getMessage());
		}
	}

	protected Dao<FileMetadata> getDao() {
		return DaoFactory.create(FileMetadata.class);
	}

	private boolean valid = false;

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	public boolean exsits(String id) {
		
		File file = null;
		if (StringUtils.isNotEmpty(id)) {
			
			if (id.length() > 3) {
				// 目录大小写不敏感
				String prefix = id.substring(0, 3);
				File folder = new File(root, prefix);
				if (!folder.exists()){
					return false;
				}
				// 必须是个文件夹
				if (!folder.isDirectory()) {
					LOGGER.error("文件服务器根目录下的[{}]应该是个目录！", prefix);
					return false;
				}
				file = new File(folder, id);
			}
		}
		
		if(null==file) return false;
		
		return file.exists();
	}

	@Override
	public FileMetadata contains(String digest) {
		FileMetadata metadata = new FileMetadata();
		metadata.setDigest(digest);
		List<FileMetadata> exsits = getDao().select(metadata);
		// TODO 先不处理link的源文件被删除的问题
		if(exsits.size()!=0){
			return exsits.get(0);
		}
		return null ;
	}

	@Override
	public String link(FileMetadata metadata)
			throws FileNotExsitException {
		metadata.setId(createId()) ;
		getDao().insert(metadata);
		return metadata.getId();
	}

	public String createId() {
		return IDGenerator.INSTANCE.createUUID();
	}

	@Override
	public FileMetadata add(InputStream content, FileMetadata metadata)
			throws IOException {
		if (content instanceof SimpleFSInputStream) {
			String id = ((SimpleFSInputStream) content).getId();
			metadata.setId(id);
			metadata.setFileid(id);
			getDao().update(metadata);
			// 本来就是文件系统内部的文件，不需要再次添加，仅更新元数据
			// TODO 确认是不是会有必须重新添加的情况
			return metadata;
		}
		String id;
		File file;
		do {
			id = createId();
			file = getFile(id);
		} while (file.exists() && file.isFile());

		// 只有在文件已存在时才会返回false
		file.createNewFile();
		try {
			write(file, content);
		} catch (IOException exception) {
			LOGGER.error(
					"写入文件" + metadata.getName() + "时发生了异常："
							+ exception.getMessage(), exception);
		}
		metadata.setId(id);
		// 新文件的元数据id与文件id一致
		metadata.setFileid(id);

		if (StringUtils.isEmpty(metadata.getMimetype())) {
			MediaType type = ContentTypeUtil.getContentType(
					new BufferedInputStream(content), metadata.getName());
			metadata.setMimetype(type.toString());
		}

		getDao().insert(metadata);
		return metadata;
	}

	/**
	 * 获取文件id对应的文件句柄，不负责创建文件
	 * 
	 * @param id
	 *            文件id
	 * @return 文件句柄，文件有可能不存在
	 */
	public File getFile(String id) {
		File file = null;
		if (StringUtils.isNotEmpty(id)) {
			if (id.length() > 3) {
				// 目录大小写不敏感
				String prefix = id.substring(0, 3);
				File folder = new File(root, prefix);
				if (!folder.exists()) {
					folder.mkdirs();
				}
				// 必须是个文件夹
				if (!folder.isDirectory()) {
					LOGGER.error("文件服务器根目录下的[{}]应该是个目录！", prefix);
					return null;
				}
				file = new File(folder, id);
			}
		}
		return file;
	}

	/**
	 * 将内存写入目标文件
	 * 
	 * @param targetFile
	 *            目标文件，最好是空文件
	 * @param in
	 *            内容
	 * @throws IOException
	 *             写入过程中发生异常
	 */
	private void write(File targetFile, InputStream in) throws IOException {
		FileOutputStream outputStream = new FileOutputStream(targetFile);
		try {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = in.read(buffer)) != -1) {
				outputStream.write(buffer, 0, length);
			}
		} catch (IOException exception) {
			throw exception;
		} finally {
			outputStream.close();
		}
	}

	@Override
	public boolean update(String id, InputStream content) throws IOException {
		File file = getFile(id);
		if (!file.exists() || file.isDirectory()) {
			throw new FileNotExsitException(id);
		}
		file.delete();
		file.createNewFile();
		try {
			write(file, content);
			return true;
		} catch (IOException exception) {
			LOGGER.error("更新文件" + id + "时发生了异常：" + exception.getMessage());
			return false;
		}
	}

	@Override
	public boolean delete(String id, boolean force) {
		File file = getFile(id);
		if (file.exists() && file.isFile()) {
			boolean success = file.delete();
			deleteFromDB(id);
			return success;
		}
		return false;
	}

	/**
	 * 从数据库中删除指定id的文件元数据
	 * 
	 * @param id
	 *            文件id
	 */
	private void deleteFromDB(String id) {
		FileMetadata metadata = new FileMetadata();
		metadata.setId(id);
		getDao().delete(metadata);
	}

	@Override
	public InputStream get(String id) throws FileNotExsitException {
		File file = getFile(id);
		if (file != null && file.exists() && file.isFile()) {
			try {
				return new SimpleFSInputStream(id, file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		//不存在，使用fileid去查询
		FileMetadata fm=this.getMetaData(id);
		if(null!=fm&&StringUtils.isNotEmpty(fm.getFileid())){
			file = getFile(fm.getFileid());
			if (file != null && file.exists() && file.isFile()) {
				try {
					 return new SimpleFSInputStream(id,file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		throw new FileNotExsitException(id);
	}

	public FileMetadata getMetaData(String id) throws FileNotExsitException {
		FileMetadata metadata = new FileMetadata();
		metadata.setId(id);
		return getDao().selectOne(metadata);
	}
	
	/**
	 * 仿问次数累加
	 * @param id
	 */
	public void addVisitCount(String id) {
		FileMetadata metadata = new FileMetadata();
		metadata.setId(id);
		metadata=getDao().selectOne(metadata);
		metadata.setVisitCount(metadata.getVisitCount()+1);
		getDao().update(metadata);
	}

	@Override
	public String getUrl(String path) throws FileNotExsitException {
		return path+"/"+getMetaData(path).getName();
	}

	@Override
	public String parsePath(String urlPath) {
		String reUrl=urlPath;
		if(reUrl.indexOf("/")!=-1){
			reUrl=urlPath.substring(0,urlPath.indexOf("/"));
		}
		return reUrl;
	}
	
}
