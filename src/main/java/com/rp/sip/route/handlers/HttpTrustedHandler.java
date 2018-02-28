package com.rp.sip.route.handlers;

import java.security.cert.X509Certificate;

/**
 * Created by cheungrp on 18/2/26.
 */
public interface HttpTrustedHandler {
    boolean isTrusted(X509Certificate[] x509Certificates, String authType);
}
