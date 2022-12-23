package me.illgilp.worldeditglobalizer.proxy.core.intake.parametric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.PARAMETER, ElementType.FIELD })
public @interface Translated {

    TranslationKey value();

}
