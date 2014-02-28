package org.wisdom.content.codecs;

import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.wisdom.api.http.EncodingNames;

@Component
@Instantiate
@Provides
public class DeflateCodec extends AbstractDefInfCodec {

	@Override
	public String getEncodingType() {
		return EncodingNames.DEFLATE;
	}

	@Override
	public String getContentEncodingHeaderValue() {
		return EncodingNames.DEFLATE;
	}
	
	@Override
	public Class<? extends DeflaterOutputStream> getEncoderClass() {
		return DeflaterOutputStream.class;
	}

	@Override
	public Class<? extends InflaterInputStream> getDecoderClass() {
		return InflaterInputStream.class;
	}
}
