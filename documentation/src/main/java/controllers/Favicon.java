package controllers;

import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import org.wisdom.api.annotations.Route;
import org.wisdom.api.http.HttpMethod;
import org.wisdom.api.http.Result;

@Controller
public class Favicon extends DefaultController{
	
	@Route(method = HttpMethod.GET, uri = "/favicon.ico")
	public Result getFavicon(){
		return redirect("/assets/images/favicon.png");
	}
}
