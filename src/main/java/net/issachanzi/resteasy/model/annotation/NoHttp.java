package net.issachanzi.resteasy.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field in an EasyModel subclass which should not be exposed via the
 * REST API
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHttp { }
