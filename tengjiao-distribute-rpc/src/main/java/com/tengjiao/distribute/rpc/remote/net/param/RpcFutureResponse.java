package com.tengjiao.distribute.rpc.remote.net.param;

import com.tengjiao.distribute.rpc.RpcException;
import com.tengjiao.distribute.rpc.remote.invoker.RpcInvoker;
import com.tengjiao.distribute.rpc.remote.invoker.call.RpcInvokeCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * call back future
 *
 * @author xuxueli 2015-11-5 14:26:37
 */
public class RpcFutureResponse implements Future<RpcResponse> {

	private RpcInvoker invokerFactory;

	// net data
	private RpcRequest request;
	private RpcResponse response;

	// future lock
	private boolean done = false;
	private Object lock = new Object();

	// callback, can be null
	private RpcInvokeCallback invokeCallback;


	public RpcFutureResponse(final RpcInvoker invokerFactory, RpcRequest request, RpcInvokeCallback invokeCallback) {
		this.invokerFactory = invokerFactory;
		this.request = request;
		this.invokeCallback = invokeCallback;

		// set-InvokerFuture
		setInvokerFuture();
	}


	// ---------------------- response pool ----------------------

	public void setInvokerFuture(){
		this.invokerFactory.setInvokerFuture(request.getRequestId(), this);
	}
	public void removeInvokerFuture(){
		this.invokerFactory.removeInvokerFuture(request.getRequestId());
	}


	// ---------------------- get ----------------------

	public RpcRequest getRequest() {
		return request;
	}
	public RpcInvokeCallback getInvokeCallback() {
		return invokeCallback;
	}


	// ---------------------- for invoke back ----------------------

	public void setResponse(RpcResponse response) {
		this.response = response;
		synchronized (lock) {
			done = true;
			lock.notifyAll();
		}
	}


	// ---------------------- for invoke ----------------------

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		// TODO
		return false;
	}

	@Override
	public boolean isCancelled() {
		// TODO
		return false;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public RpcResponse get() throws InterruptedException, ExecutionException {
		try {
			return get(-1, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			throw new RpcException(e);
		}
	}

	@Override
	public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if (!done) {
			synchronized (lock) {
				try {
					if (timeout < 0) {
						lock.wait();
					} else {
						long timeoutMillis = (TimeUnit.MILLISECONDS==unit)?timeout:TimeUnit.MILLISECONDS.convert(timeout , unit);
						lock.wait(timeoutMillis);
					}
				} catch (InterruptedException e) {
					throw e;
				}
			}
		}

		if (!done) {
			throw new RpcException("request timeout at:"+ System.currentTimeMillis() +", request:" + request.toString());
		}
		return response;
	}


}
