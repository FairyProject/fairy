package io.fairyproject.internal;

import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This is an internal meta annotation to identify compiler this class is made for fairy.
 * Please do NOT use this class anywhere else so fairy-gradle-plugin wouldn't messed up on compile.
 */
@ApiStatus.Internal
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FairyInternalIdentityMeta {
}
