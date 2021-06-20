package com.tengjiao.distribute.rpc.sample.server.service;

import com.tengjiao.distribute.rpc.sample.api.DemoService;
import com.tengjiao.distribute.rpc.sample.api.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @author
 */
public class DemoServiceImpl implements DemoService {
    private static Logger logger = LoggerFactory.getLogger(DemoServiceImpl.class);

    @Override
    public UserDTO sayHi(String name) {
        String word = MessageFormat.format("Hi {0}, from {1} as {2}",
                name, DemoServiceImpl.class.getName(), String.valueOf(System.currentTimeMillis()));

        if ("error".equalsIgnoreCase(name)) {
            throw new RuntimeException("test exception.");
        }

        UserDTO userDTO = new UserDTO(name, word);
        logger.info(userDTO.toString());

        return userDTO;
    }
}
