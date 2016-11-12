package com.chinacreator.c2.fs;

import java.io.Serializable;

import com.chinacreator.c2.annotation.Column;
import com.chinacreator.c2.annotation.ColumnType;
import com.chinacreator.c2.annotation.Entity;

/**
 * 文件元数据
 * @author 
 * @generated
 */
@Entity(id = "entity:com.chinacreator.c2.fs.FileMetadata", table = "td_c2_file_metadata", autoIndex = false)
public class FileMetadata implements Serializable {
	private static final long serialVersionUID = 696586089594880L;
	/**
	 *元数据id
	 */
	@Column(id = "id_", type = ColumnType.uuid, datatype = "char22")
	private java.lang.String id;

	/**
	 *文件id
	 */
	@Column(id = "fileid", datatype = "char22")
	private java.lang.String fileid;

	/**
	 *文件名称
	 */
	@Column(id = "name", datatype = "string256")
	private java.lang.String name;

	/**
	 *文件MIMETYPE
	 */
	@Column(id = "mimetype", datatype = "string128")
	private java.lang.String mimetype;

	/**
	 *文件大小
	 */
	@Column(id = "filesize", datatype = "long")
	private java.lang.Long filesize;

	/**
	 *文件摘要
	 */
	@Column(id = "digest", datatype = "string128")
	private java.lang.String digest;

	/**
	 *仿问次数
	 */
	@Column(id = "visit_count", datatype = "int")
	private java.lang.Integer visitCount;

	/**
	 *路径
	 */
	@Column(id = "path", datatype = "string2000")
	private java.lang.String path;

	/**
	 * 设置元数据id
	 */
	public void setId(java.lang.String id) {
		this.id = id;
	}

	/**
	 * 获取元数据id
	 */
	public java.lang.String getId() {
		return id;
	}

	/**
	 * 设置文件id
	 */
	public void setFileid(java.lang.String fileid) {
		this.fileid = fileid;
	}

	/**
	 * 获取文件id
	 */
	public java.lang.String getFileid() {
		return fileid;
	}

	/**
	 * 设置文件名称
	 */
	public void setName(java.lang.String name) {
		this.name = name;
	}

	/**
	 * 获取文件名称
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * 设置文件MIMETYPE
	 */
	public void setMimetype(java.lang.String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * 获取文件MIMETYPE
	 */
	public java.lang.String getMimetype() {
		return mimetype;
	}

	/**
	 * 设置文件大小
	 */
	public void setFilesize(java.lang.Long filesize) {
		this.filesize = filesize;
	}

	/**
	 * 获取文件大小
	 */
	public java.lang.Long getFilesize() {
		return filesize;
	}

	/**
	 * 设置文件摘要
	 */
	public void setDigest(java.lang.String digest) {
		this.digest = digest;
	}

	/**
	 * 获取文件摘要
	 */
	public java.lang.String getDigest() {
		return digest;
	}

	/**
	 * 设置仿问次数
	 */
	public void setVisitCount(java.lang.Integer visitCount) {
		this.visitCount = visitCount;
	}

	/**
	 * 获取仿问次数
	 */
	public java.lang.Integer getVisitCount() {
		return visitCount;
	}

	/**
	 * 设置路径
	 */
	public void setPath(java.lang.String path) {
		this.path = path;
	}

	/**
	 * 获取路径
	 */
	public java.lang.String getPath() {
		return path;
	}
}
