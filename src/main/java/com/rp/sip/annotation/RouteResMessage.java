package com.rp.sip.annotation;

import java.lang.annotation.*;

/**
 * Created by cheungrp on 17/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SipMessage
@Inherited
public @interface RouteResMessage {
    String value() default "";
}
