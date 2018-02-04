package com.rp.sip.classloader;



import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by cheungrp on 18/2/2.
 */
@Component("sipUserClassloader")
public class SipUserClassloader extends URLClassLoader {

    public SipUserClassloader(){
        this(new URL[]{});
    }


    public SipUserClassloader(URL[] urls) {
        super(urls);
    }


    public void addURL(String jarPath) throws MalformedURLException {
        super.addURL(new URL("file:"+jarPath));
    }


}
