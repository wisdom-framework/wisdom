package org.wisdom.content.codecs;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class GzipCodec extends AbstractDefInfCodec {
	
	@Override
	public String getEncodingType(){
		return EncodingNames.GZIP;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return EncodingNames.GZIP;
	}

	@Override
	public Class<? extends DeflaterOutputStream> getEncoderClass() {
		return GZIPOutputStream.class;
	}

	@Override
	public Class<? extends InflaterInputStream> getDecoderClass() {
		return GZIPInputStream.class;
	}
}
