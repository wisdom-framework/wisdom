package org.wisdom.content.encoders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wisdom.api.content.ContentCodec;
import org.wisdom.api.http.EncodingNames;

public class CodecsTest {
	
	@Test
	public void testGzipCodec(){
		ContentCodec codec = testCodec(GzipCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(EncodingNames.GZIP);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.GZIP);
	}
	
	@Test
	public void testDeflateCodec(){
		ContentCodec codec = testCodec(DeflateCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(EncodingNames.DEFLATE);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.DEFLATE);
	}
	
	@Test
	public void testIdentityCodec(){
		ContentCodec codec = testCodec(IdentityCodec.class);
		if(codec == null)
			return;
		
		assertThat(codec.getContentEncodingHeaderValue()).isEqualTo(null);
		assertThat(codec.getEncodingType()).isEqualTo(EncodingNames.IDENTITY);
	}
	
	private ContentCodec testCodec(Class<? extends ContentCodec> codecClass) {
		String data = "abcdefghijklmonpqrstuvwxyz";
		
		ContentCodec codec;
		try {
			codec = codecClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e1) {
			fail("Unable to instantiate " + codecClass);
			e1.printStackTrace();
			return null;
		}
		
		InputStream inputData, encodedData = null, decodedData = null;
		inputData = IOUtils.toInputStream(data);
		
		try {
			encodedData = codec.encode(inputData);
			decodedData = codec.decode(encodedData);
			assertThat(IOUtils.toString(decodedData)).isEqualTo(data);
		} catch (IOException e) {
			assertThat(false);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(inputData);
			IOUtils.closeQuietly(encodedData);
			IOUtils.closeQuietly(decodedData);
		}
		
		return codec;
	}
}
