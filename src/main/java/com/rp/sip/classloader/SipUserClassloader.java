package com.rp.sip.classloader;

import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
public class SIPUserClassLoader extends URLClassLoader {

	public SIPUserClassLoader(ClassLoader parent) {
		this(new URL[] {}, parent);
	}

	public SIPUserClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public void addURL(String jarPath) throws MalformedURLException {
		super.addURL(new URL("file:" + jarPath));
	}

}
