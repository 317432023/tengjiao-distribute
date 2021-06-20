package com.tengjiao.distribute.rpc.remote.net;

import com.tengjiao.distribute.rpc.remote.net.param.BaseCallback;
import com.tengjiao.distribute.rpc.remote.provider.RpcProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * server
 *
 * @author
 */
public abstract class Server {
	protected static final Logger logger = LoggerFactory.getLogger(Server.class);


	private BaseCallback startedCallback = null;
	private BaseCallback stopedCallback = null;

	public void setStartedCallback(BaseCallback startedCallback) {
		this.startedCallback = startedCallback;
	}

	public void setStopedCallback(BaseCallback stopedCallback) {
		this.stopedCallback = stopedCallback;
	}


	/**
	 * start server
	 *
	 * @param rpcProvider
	 * @throws Exception
	 */
	public abstract void start(final RpcProvider rpcProvider) throws Exception;

	/**
	 * callback when started
	 */
	public void onStarted() {
		if (startedCallback != null) {
			try {
				startedCallback.run();
			} catch (Exception e) {
				logger.error(">>>>>>>>>>> server startedCallback error.", e);
			}
		}
	}

	/**
	 * stop server
	 *
	 * @throws Exception
	 */
	public abstract void stop() throws Exception;

	/**
	 * callback when stoped
	 */
	public void onStoped() {
		if (stopedCallback != null) {
			try {
				stopedCallback.run();
			} catch (Exception e) {
				logger.error(">>>>>>>>>>> server stopedCallback error.", e);
			}
		}
	}

}
