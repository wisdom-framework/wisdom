package org.wisdom.content.encoders;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.encoder.AllowEncoding;
import org.wisdom.api.annotations.encoder.DenyEncoding;
import org.wisdom.api.http.Result;

@DenyEncoding
public class FakeDenyController extends DefaultController {
	@DenyEncoding
	public Result deny(){
		return ok();
	}
	@AllowEncoding
	public Result allow(){
		return ok();
	}
	
	public Result noAnnotation(){
		return ok();
	}
}
