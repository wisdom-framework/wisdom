package org.wisdom.content.encoders;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.encoder.AllowEncoding;
import org.wisdom.api.http.Result;

public class FakeSizeController extends DefaultController{

	@AllowEncoding(maxSize=1000000*1024, minSize=1)
	public Result changeSize(){
		return ok();
	}
	
	public Result noAnnotation(){
		return ok();
	}
}
