package com.tengjiao.distribute.rpc.sample.api;


import com.tengjiao.distribute.rpc.sample.api.dto.UserDTO;

/**
 * Demo API
 * @author
 */
public interface DemoService {

	public UserDTO sayHi(String name);

}
