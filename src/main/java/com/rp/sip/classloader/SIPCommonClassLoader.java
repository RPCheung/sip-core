package com.rp.sip.classloader;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

@Component("commonClassLoader")
public class SIPCommonClassLoader extends URLClassLoader {

	public SIPCommonClassLoader() {
		this(new URL[] {}, ClassLoader.getSystemClassLoader());
	}

	public SIPCommonClassLoader(URL[] urls, ClassLoader classLoader) {
		super(urls, classLoader);
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

	public void addURL(String jarPath) throws MalformedURLException {
		super.addURL(new URL("file:" + jarPath));
	}

}
