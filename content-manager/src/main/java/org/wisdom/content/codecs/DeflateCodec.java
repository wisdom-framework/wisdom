package org.wisdom.content.codecs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class DeflateCodec implements ContentCodec {
	//TODO What level to set Deflater codec ?
	Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

	@Override
	public InputStream encode(InputStream toEncode) throws IOException {
		ByteArrayOutputStream bout =  new ByteArrayOutputStream();

		OutputStream dout = new DeflaterOutputStream(bout);
		dout.write(IOUtils.toByteArray(toEncode));
		dout.flush();
		dout.close();
		toEncode.close();

		bout.flush();
		InputStream encoded = new ByteArrayInputStream(bout.toByteArray());
		bout.close();
		return encoded;
	}
	
	@Override
	public InputStream decode(InputStream toDecode) throws IOException {
		return new InflaterInputStream(toDecode);
	}

	@Override
	public String getEncodingType() {
		return EncodingNames.DEFLATE;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return EncodingNames.DEFLATE;
	}
}
