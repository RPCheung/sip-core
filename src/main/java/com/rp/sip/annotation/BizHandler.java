package com.rp.sip.annotation;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * Created by cheungrp on 17/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Lazy
@Documented
@Scope("prototype")
@Component
public @interface BizHandler {
    String value() default "";
}
