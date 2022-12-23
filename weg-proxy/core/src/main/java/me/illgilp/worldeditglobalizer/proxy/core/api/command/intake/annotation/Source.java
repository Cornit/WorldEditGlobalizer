package me.illgilp.worldeditglobalizer.proxy.core.api.command.intake.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation.Classifier;

@Classifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Source {
}
