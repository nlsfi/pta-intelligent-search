package fi.maanmittauslaitos.pta.search.csw;

import fi.maanmittauslaitos.pta.search.HarvestingException;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class HarvesterInputStream extends FilterInputStream {

	private HarvesterInputStream(InputStream in) {
		super(in);
	}

	public static HarvesterInputStream wrap(InputStream inputStream) {
		return new HarvesterInputStream(inputStream);
	}

	@Override
	public int read() {
		try {
			return super.read();
		} catch (IOException e) {
			throw new HarvestingException(e);
		}
	}

	@Override
	public int read(byte[] b) {
		try {
			return super.read(b);
		} catch (IOException e) {
			throw new HarvestingException();
		}
	}

	@Override
	public int read(byte[] b, int off, int len) {
		try {
			return super.read(b, off, len);
		} catch (IOException e) {
			throw new HarvestingException();
		}
	}
}
