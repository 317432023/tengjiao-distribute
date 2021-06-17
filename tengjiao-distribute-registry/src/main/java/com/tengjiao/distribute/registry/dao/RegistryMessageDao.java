package com.tengjiao.distribute.registry.dao;

import com.tengjiao.distribute.registry.model.RegistryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RegistryMessageDao {

    int add(@Param("registryMessage") RegistryMessage registryMessage);

    List<RegistryMessage> findMessage(@Param("excludeIds") List<Integer> excludeIds);

    int cleanMessage(@Param("messageTimeout") int messageTimeout);

}
