package com.tengjiao.distribute.rpc.remote.invoker.route.impl;

import com.tengjiao.distribute.rpc.remote.invoker.route.RpcLoadBalance;

import java.util.Random;
import java.util.TreeSet;

/**
 * random
 *
 * @author xuxueli 2018-12-04
 */
public class RpcLoadBalanceRandomStrategy extends RpcLoadBalance {

    private Random random = new Random();

    @Override
    public String route(String serviceKey, TreeSet<String> addressSet) {
        // arr
        String[] addressArr = addressSet.toArray(new String[addressSet.size()]);

        // random
        String finalAddress = addressArr[random.nextInt(addressSet.size())];
        return finalAddress;
    }

}
