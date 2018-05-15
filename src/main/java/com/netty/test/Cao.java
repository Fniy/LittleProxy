package com.netty.test;

import io.netty.handler.codec.http.FullHttpRequest;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Cao {
    private final ConcurrentHashMap<String, FullHttpRequest> requestMap = new ConcurrentHashMap<>();
    private static Cao instance = new Cao();

    public void put(String key, FullHttpRequest value){
        requestMap.put(key, value);
    }

    public FullHttpRequest get(String key){
        return requestMap.get(key);
    }

    public HashMap<String, FullHttpRequest> copy(){
        return new HashMap(requestMap);
    }

    public int size(){
        return requestMap.values().size();
    }

    public static Cao getInstance() {
        return instance;
    }
}
