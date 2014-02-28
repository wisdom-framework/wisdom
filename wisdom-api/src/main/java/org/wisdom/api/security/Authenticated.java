package org.wisdom.api.security;

import org.wisdom.api.annotations.Interception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Interception
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {

    Class<? extends Authenticator> value() default Authenticator.class;
}
