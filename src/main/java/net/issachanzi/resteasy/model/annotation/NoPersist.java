package net.issachanzi.resteasy.model.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field in an EasyModel subclass that should not be saved to the
 * database
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPersist { }
