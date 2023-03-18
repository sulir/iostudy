package com.sulir.github.iostudy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Program {
    String name();
    String arguments() default "";
}
