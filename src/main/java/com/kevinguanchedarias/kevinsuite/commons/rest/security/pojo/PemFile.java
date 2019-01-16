/**
 * 
 */
package com.kevinguanchedarias.kevinsuite.commons.rest.security.pojo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

/**
 *
 * @since 0.2.0
 * @see https://www.txedo.com/blog/java-read-rsa-keys-pem-file/
 * @author Kevin Guanche Darias <kevin@kevinguanchedarias.com>
 */
public class PemFile {
	private PemObject pemObject;

	public PemFile(String filename) throws IOException {
		PemReader pemReader = new PemReader(new InputStreamReader(new FileInputStream(filename)));
		try {
			this.pemObject = pemReader.readPemObject();
		} finally {
			pemReader.close();
		}
	}

	public PemObject getPemObject() {
		return pemObject;
	}
}
