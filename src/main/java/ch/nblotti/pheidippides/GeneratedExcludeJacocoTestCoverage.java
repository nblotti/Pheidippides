package ch.nblotti.pheidippides;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface GeneratedExcludeJacocoTestCoverage {
}
