package com.salutron.lifetrakwatchapp.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import com.salutron.lifetrakwatchapp.model.BaseModel;

/**
 * Annotation for declaring column in a table
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataColumn {
	public String name() default "";
	public boolean isPrimary() default false;
	public boolean isForeign() default false;
	public Class<? extends BaseModel> model() default BaseModel.class;
}
