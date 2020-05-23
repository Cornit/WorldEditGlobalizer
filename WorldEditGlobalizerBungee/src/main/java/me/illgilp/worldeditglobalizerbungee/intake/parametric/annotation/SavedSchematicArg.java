package me.illgilp.worldeditglobalizerbungee.intake.parametric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.illgilp.intake.parametric.annotation.Classifier;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PARAMETER, ElementType.FIELD })
@Classifier
public @interface SavedSchematicArg {
}
