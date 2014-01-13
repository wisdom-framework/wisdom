package org.wisdom.content.encoders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.content.ContentEncoder;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class GzipEncoder implements ContentEncoder {

	@Override
	public InputStream encode(InputStream toEncode) throws IOException {
		ByteArrayOutputStream bout =  new ByteArrayOutputStream();

		OutputStream gzout = new GZIPOutputStream(bout);
		gzout.write(IOUtils.toByteArray(toEncode));
		gzout.flush();
		gzout.close();
		toEncode.close();

		bout.flush();
		InputStream encoded = new ByteArrayInputStream(bout.toByteArray());
		bout.close();
		return encoded;
	}
	
	@Override
	public String getEncodingType(){
		return EncodingNames.GZIP;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return EncodingNames.GZIP;
	}
}
