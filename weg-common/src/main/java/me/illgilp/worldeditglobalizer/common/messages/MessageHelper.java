package me.illgilp.worldeditglobalizer.common.messages;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import me.illgilp.worldeditglobalizer.common.messages.translation.TranslationKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.UTF8ResourceBundleControl;

public class MessageHelper {

    private static final MiniMessage miniMessageInstance = MiniMessage.builder()
        .tags(TagResolver.resolver(
            StandardTags.defaults(),
            TagResolver.resolver("plural", MessageHelper::pluralTag),
            TagResolver.resolver("format_bytes", MessageHelper::formatBytesTag)
        ))
        .build();

    private static final String bundleBaseName = "messages";
    private static final Locale[] bundleLocals = {
        Locale.US,
        Locale.GERMANY
    };

    private static final Locale fallbackLocale = Locale.US;

    private static final Map<Locale, ResourceBundle> bundles = new HashMap<>();

    public static void loadMessages() throws IOException, IllegalAccessException, InstantiationException {
        bundles.clear();
        for (Locale bundleLocal : bundleLocals) {
            ResourceBundle bundle = UTF8ResourceBundleControl.get().newBundle(
                "lang/" + bundleBaseName, bundleLocal,
                "java.properties",
                MessageHelper.class.getClassLoader(),
                true
            );
            bundles.put(bundleLocal, bundle);
        }
    }

    private static String getTranslation(Locale locale, TranslationKey translationKey) {
        if (locale == null) {
            locale = fallbackLocale;
        }
        return Optional.ofNullable(bundles.getOrDefault(locale, bundles.get(fallbackLocale)))
            .map(resourceBundle -> resourceBundle.getString(translationKey.getKey()))
            .orElse(translationKey.getKey());
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Tag pluralTag(ArgumentQueue args, Context ctx) {
        String numberStr = args
            .popOr("The <plural> Tag needs at least 2 arguments: the name of the variable and the plural extension")
            .value();

        String pluralStr = args
            .popOr("The <plural> Tag needs at least 2 arguments: the name of the variable and the plural extension")
            .value();

        String singularStr = null;
        if (args.hasNext()) {
            singularStr = pluralStr;
            pluralStr = args.pop().value();
        }

        String numberContent = ((TextComponent) ctx.deserialize(String.format("<%s>", numberStr))).content();
        BigDecimal number;
        try {
            number = new BigDecimal(numberContent);
        } catch (NumberFormatException e) {
            throw ctx.newException(String.format("'%s' is not a number", numberContent));
        }

        return Tag.inserting(number.compareTo(BigDecimal.ONE) == 0
            ? (singularStr == null ? Component.empty() : ctx.deserialize(singularStr))
            : ctx.deserialize(pluralStr));
    }

    private static Tag prefixTag(Supplier<Locale> locale, ArgumentQueue args, Context ctx) {
        return Tag.inserting(
            MessageHelper.builder()
                .locale(locale.get())
                .translation(TranslationKey.PREFIX)
                .build()
        );
    }

    private static Tag formatBytesTag(ArgumentQueue args, Context ctx) {
        String numberStr = args
            .popOr("The <format_bytes> Tag needs at least 1 argument: the name of the variable")
            .value();

        String numberContent = ((TextComponent) ctx.deserialize(String.format("<%s>", numberStr))).content();
        BigDecimal number;
        try {
            number = new BigDecimal(numberContent);
        } catch (NumberFormatException e) {
            throw ctx.newException(String.format("'%s' is not a number", numberContent));
        }

        final String[] units = { "B", "KiB", "MiB", "GiB", "TiB" };

        String unit = units[0];
        for (int i = 1; i < units.length; i++) {
            final BigDecimal n = number.divide(BigDecimal.valueOf(1024).pow(i), RoundingMode.HALF_UP);
            if (n.compareTo(BigDecimal.ONE) <= -1) {
                unit = units[i - 1];
                number = number.divide(BigDecimal.valueOf(1024).pow(i - 1), RoundingMode.HALF_UP);
                break;
            }
            if (i == units.length - 1) {
                unit = units[i];
                number = n;
                break;
            }
        }
        return Tag.inserting(Component.text(number.toPlainString() + " " + unit));
    }

    public static class Builder {
        private Locale translationLocale = null;
        private final List<Supplier<Component>> components = new LinkedList<>();
        private final List<TagResolver> tagResolvers = new ArrayList<>();

        private Builder() {
            this.tagResolvers.add(TagResolver.resolver("prefix", (argumentQueue, context) ->
                MessageHelper.prefixTag(() -> this.translationLocale, argumentQueue, context)));
        }

        public Builder locale(Locale locale) {
            if (locale == null) {
                return this;
            }

            this.translationLocale = locale;
            return this;
        }

        public Locale getLocale() {
            return translationLocale;
        }

        public Builder receiver(Pointered receiver) {
            return this.locale(receiver.getOrDefault(Identity.LOCALE, fallbackLocale));
        }

        public Builder tagResolver(TagResolver tagResolver) {
            this.tagResolvers.add(tagResolver);
            return this;
        }

        public Builder lazyPlaceholder(String key, Builder builder) {
            if (builder == null) {
                return this;
            }
            this.tagResolver(TagResolver.resolver(key, (argumentQueue, context) ->
                Tag.inserting(builder.locale(this.translationLocale).build())));
            return this;
        }

        public Builder component(Component component) {
            if (component == null) {
                return this;
            }
            this.components.add(() -> component);
            return this;
        }

        public Builder component(Builder builder) {
            if (builder == null) {
                return this;
            }
            this.components.add(() -> builder.locale(this.translationLocale).build());
            return this;
        }

        public Builder lazyComponent(Function<Builder, Component> function) {
            if (function == null) {
                return this;
            }
            this.components.add(() -> function.apply(this));
            return this;
        }

        public Builder translation(TranslationKey key) {
            this.components.add(() ->
                miniMessageInstance.deserialize(getTranslation(this.translationLocale, key), TagResolver.resolver(this.tagResolvers))
            );
            return this;
        }

        public Builder miniMessage(String message) {
            this.components.add(() ->
                miniMessageInstance.deserialize(message, TagResolver.resolver(this.tagResolvers))
            );
            return this;
        }

        public void sendMessageTo(Audience audience) {
            this.receiver(audience);
            audience.sendMessage(this.build());
        }

        public void sendActionBarTo(Audience audience) {
            this.receiver(audience);
            audience.sendActionBar(this.build());
        }

        public Component build() {
            return Component.join(
                JoinConfiguration.noSeparators(),
                this.components.stream()
                    .map(Supplier::get)
                    .collect(Collectors.toList())
            ).compact();
        }

        public String buildPlain() {
            return PlainTextComponentSerializer.plainText().serialize(build());
        }

        public static Collector<Builder, ?, Builder> toBuilder(Builder separator) {
            return Collector.of(
                MessageHelper::builder,
                new BiConsumer<Builder, Builder>() {

                    boolean first = true;

                    @Override
                    public void accept(Builder builder, Builder builder2) {
                        if (first) {
                            first = false;
                        } else {
                            builder.component(separator);
                        }
                        builder.component(builder2);
                    }
                },
                Builder::component
            );
        }

        public static Collector<Builder, ?, Builder> toBuilder(Component separator) {
            return toBuilder(MessageHelper.builder().component(separator));
        }

    }

}
