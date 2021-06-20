package com.tengjiao.distribute.rpc.sample.client;


import com.tengjiao.distribute.rpc.remote.invoker.RpcInvoker;
import com.tengjiao.distribute.rpc.remote.invoker.call.CallType;
import com.tengjiao.distribute.rpc.remote.invoker.call.RpcInvokeCallback;
import com.tengjiao.distribute.rpc.remote.invoker.call.RpcInvokeFuture;
import com.tengjiao.distribute.rpc.remote.invoker.reference.RpcReferenceBean;
import com.tengjiao.distribute.rpc.remote.invoker.route.LoadBalance;
import com.tengjiao.distribute.rpc.remote.net.impl.netty.client.NettyClient;
import com.tengjiao.distribute.rpc.sample.api.DemoService;
import com.tengjiao.distribute.rpc.sample.api.dto.UserDTO;
import com.tengjiao.distribute.rpc.serialize.impl.HessianSerializer;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author
 */
public class RpcClientApplication {

	public static void main(String[] args) throws Exception {

		/*String serviceKey = RpcProviderFactory.makeServiceKey(DemoService.class.getName(), null);
		RpcInvoker.getInstance().getServiceRegistry().registry(new HashSet<String>(Arrays.asList(serviceKey)), "127.0.0.1:7080");*/

		// test
		testSYNC();
		testFUTURE();
		testCALLBACK();
		testONEWAY();

		TimeUnit.SECONDS.sleep(2);

		// stop client invoker factory (default by getInstance, exist inner thread, need destroy)
		RpcInvoker.getInstance().stop();

	}



	/**
	 * CallType.SYNC
	 */
	public static void testSYNC() throws Exception {
		// init client
		RpcReferenceBean referenceBean = new RpcReferenceBean();
		referenceBean.setClient(NettyClient.class);
		referenceBean.setSerializer(HessianSerializer.class);
		referenceBean.setCallType(CallType.SYNC);
		referenceBean.setLoadBalance(LoadBalance.ROUND);
		referenceBean.setIface(DemoService.class);
		referenceBean.setVersion(null);
		referenceBean.setTimeout(500);
		referenceBean.setAddress("127.0.0.1:7080");
		referenceBean.setAccessToken(null);
		referenceBean.setInvokeCallback(null);
		referenceBean.setInvokerFactory(null);

		DemoService demoService = (DemoService) referenceBean.getObject();

		// test
        UserDTO userDTO = demoService.sayHi("[SYNC]jack");
		System.out.println(userDTO);


		// test mult
		/*int count = 100;
		long start = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			UserDTO userDTO2 = demoService.sayHi("[SYNC]jack"+i );
			System.out.println(i + "##" + userDTO2.toString());
		}
		long end = System.currentTimeMillis();
    	System.out.println("run count:"+ count +", cost:" + (end - start));*/

	}


	/**
	 * CallType.FUTURE
	 */
	public static void testFUTURE() throws Exception {
		// client test
		RpcReferenceBean referenceBean = new RpcReferenceBean();
		referenceBean.setClient(NettyClient.class);
		referenceBean.setSerializer(HessianSerializer.class);
		referenceBean.setCallType(CallType.FUTURE);
		referenceBean.setLoadBalance(LoadBalance.ROUND);
		referenceBean.setIface(DemoService.class);
		referenceBean.setVersion(null);
		referenceBean.setTimeout(500);
		referenceBean.setAddress("127.0.0.1:7080");
		referenceBean.setAccessToken(null);
		referenceBean.setInvokeCallback(null);
		referenceBean.setInvokerFactory(null);

		DemoService demoService = (DemoService) referenceBean.getObject();

		// test
		demoService.sayHi("[FUTURE]jack" );
        Future<UserDTO> userDTOFuture = RpcInvokeFuture.getFuture(UserDTO.class);
		UserDTO userDTO = userDTOFuture.get();

		System.out.println(userDTO.toString());
	}


	/**
	 * CallType.CALLBACK
	 */
	public static void testCALLBACK() throws Exception {
		// client test
		RpcReferenceBean referenceBean = new RpcReferenceBean();
		referenceBean.setClient(NettyClient.class);
		referenceBean.setSerializer(HessianSerializer.class);
		referenceBean.setCallType(CallType.CALLBACK);
		referenceBean.setLoadBalance(LoadBalance.ROUND);
		referenceBean.setIface(DemoService.class);
		referenceBean.setVersion(null);
		referenceBean.setTimeout(500);
		referenceBean.setAddress("127.0.0.1:7080");
		referenceBean.setAccessToken(null);
		referenceBean.setInvokeCallback(null);
		referenceBean.setInvokerFactory(null);

		DemoService demoService = (DemoService) referenceBean.getObject();


        // test
        RpcInvokeCallback.setCallback(new RpcInvokeCallback<UserDTO>() {
            @Override
            public void onSuccess(UserDTO result) {
                System.out.println(result);
            }

            @Override
            public void onFailure(Throwable exception) {
                exception.printStackTrace();
            }
        });

        demoService.sayHi("[CALLBACK]jack");
	}


	/**
	 * CallType.ONEWAY
	 */
	public static void testONEWAY() throws Exception {
		// client test
		RpcReferenceBean referenceBean = new RpcReferenceBean();
		referenceBean.setClient(NettyClient.class);
		referenceBean.setSerializer(HessianSerializer.class);
		referenceBean.setCallType(CallType.ONEWAY);
		referenceBean.setLoadBalance(LoadBalance.ROUND);
		referenceBean.setIface(DemoService.class);
		referenceBean.setVersion(null);
		referenceBean.setTimeout(500);
		referenceBean.setAddress("127.0.0.1:7080");
		referenceBean.setAccessToken(null);
		referenceBean.setInvokeCallback(null);
		referenceBean.setInvokerFactory(null);

		DemoService demoService = (DemoService) referenceBean.getObject();

		// test
        demoService.sayHi("[ONEWAY]jack");
	}

}
