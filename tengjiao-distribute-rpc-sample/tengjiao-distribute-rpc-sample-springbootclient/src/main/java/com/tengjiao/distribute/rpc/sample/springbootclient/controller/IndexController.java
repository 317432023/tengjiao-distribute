package com.tengjiao.distribute.rpc.sample.springbootclient.controller;

import com.tengjiao.distribute.rpc.remote.invoker.annotation.RpcReference;
import com.tengjiao.distribute.rpc.sample.api.DemoService;
import com.tengjiao.distribute.rpc.sample.api.dto.UserDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {
	
	@RpcReference
	private DemoService demoService;


	@RequestMapping("")
	@ResponseBody
	public UserDTO http(String name) {

		try {
			return demoService.sayHi(name);
		} catch (Exception e) {
			e.printStackTrace();
			return new UserDTO(null, e.getMessage());
		}
	}

}
