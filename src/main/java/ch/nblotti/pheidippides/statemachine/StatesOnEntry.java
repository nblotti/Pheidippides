package ch.nblotti.pheidippides.statemachine;

import org.springframework.statemachine.annotation.OnStateEntry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnStateEntry
public @interface StatesOnEntry {

  STATES[] target() default {};

}
