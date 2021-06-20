package com.tengjiao.distribute.rpc.serialize;

/**
 * serializer
 * @author
 */
public interface Serializer {
	
	public <T> byte[] serialize(T obj);
	public <T> Object deserialize(byte[] bytes, Class<T> clazz);

}
