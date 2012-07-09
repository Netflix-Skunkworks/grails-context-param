package com.netflix.grails.contextParam

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * The value of this annotation indicates a parameter name that will be automatically sent along to any redirect or 
 * chain calls to this controller.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface ContextParam {
    String value()
}