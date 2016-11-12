package com.chinacreator.c2.fs.util;

import java.io.IOException;
import java.io.InputStream;

import com.chinacreator.c2.fs.FileMetadata;
import com.chinacreator.c2.fs.HTTPFileServer;
import com.chinacreator.c2.fs.exception.FileNotExsitException;

public class EmptyFileServer implements HTTPFileServer {

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public boolean exsits(String id) {
        return false;
    }

    @Override
    public FileMetadata add(InputStream content, FileMetadata metadata)
            throws IOException {
        return null;
    }

    @Override
    public boolean update(String id, InputStream content)
            throws FileNotExsitException, IOException {
        return false;
    }

    @Override
    public boolean delete(String id, boolean force) {
        return false;
    }

    @Override
    public InputStream get(String id) throws FileNotExsitException, IOException {
        return null;
    }

    @Override
    public FileMetadata getMetaData(String id) throws FileNotExsitException {
        return null;
    }

	@Override
	public String getUrl(String path) throws FileNotExsitException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String parsePath(String urlPath) {
		// TODO Auto-generated method stub
		return null;
	}
}
