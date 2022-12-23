/*
 * Intake, a command processing library
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) Intake team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.illgilp.worldeditglobalizer.proxy.core.intake;

import static me.illgilp.worldeditglobalizer.proxy.core.intake.util.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.illgilp.worldeditglobalizer.common.messages.MessageHelper;
import me.illgilp.worldeditglobalizer.common.permission.Permission;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable implementation of {@link Parameter}.
 */
public final class ImmutableParameter implements Parameter {

    private final MessageHelper.Builder name;
    @Nullable
    private final OptionType optionType;
    private final List<String> defaultValue;

    private final MessageHelper.Builder description;

    private final List<Permission> permissions;

    private ImmutableParameter(MessageHelper.Builder name, OptionType optionType, List<String> defaultValue, MessageHelper.Builder description, List<Permission> permissions) {
        this.name = name;
        this.optionType = optionType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.permissions = permissions;
    }

    @Override
    public MessageHelper.Builder getName() {
        return name;
    }

    @Override
    public OptionType getOptionType() {
        return optionType;
    }

    @Override
    public List<String> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public Optional<MessageHelper.Builder> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public List<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public String toString() {
        return "ImmutableParameter{" +
            "name=" + name.buildPlain() +
            ", optionType=" + optionType +
            ", defaultValue=" + defaultValue +
            ", description=" + Optional.ofNullable(description).map(MessageHelper.Builder::buildPlain).orElse("null") +
            ", permissions=" + permissions +
            '}';
    }

    /**
     * Creates instances of {@link ImmutableParameter}.
     *
     * <p>By default, the default value will be an empty list.</p>
     */
    public static class Builder {
        private MessageHelper.Builder name;
        private OptionType optionType;
        private List<String> defaultValue = Collections.emptyList();
        private MessageHelper.Builder description;
        private List<Permission> permissions = Collections.emptyList();

        /**
         * Get the name of the parameter.
         *
         * @return The name of the parameter
         */
        public MessageHelper.Builder getName() {
            return name;
        }

        /**
         * Set the name of the parameter.
         *
         * @param name The name of the parameter
         * @return The builder
         */
        public Builder setName(MessageHelper.Builder name) {
            checkNotNull(name, "name");
            this.name = name;
            return this;
        }

        /**
         * Gets the description of the parameter.
         *
         * @return The description of the parameter if provided
         */
        public Optional<MessageHelper.Builder> getDescription() {
            return Optional.ofNullable(description);
        }

        /**
         * Set the description of the parameter.
         *
         * @param description The description of the parameter.
         * @return The Builder
         */
        public Builder setDescription(MessageHelper.Builder description) {
            this.description = description;
            return this;
        }

        /**
         * Get the type of parameter.
         *
         * @return The type of parameter
         */
        public OptionType getOptionType() {
            return optionType;
        }

        /**
         * Set the type of parameter.
         *
         * @param optionType The type of parameter
         * @return The builder
         */
        public Builder setOptionType(OptionType optionType) {
            checkNotNull(optionType, "optionType");
            this.optionType = optionType;
            return this;
        }

        /**
         * Get the default value as a list of arguments.
         *
         * <p>An empty list implies that there is no default value.</p>
         *
         * @return The default value (one value) as a list
         */
        public List<String> getDefaultValue() {
            return defaultValue;
        }

        /**
         * Set the default value as a list of arguments.
         *
         * <p>An empty list implies that there is no default value.</p>
         *
         * @param defaultValue The default value (one value) as a list
         * @return The builder
         */
        public Builder setDefaultValue(List<String> defaultValue) {
            checkNotNull(defaultValue, "defaultValue");
            this.defaultValue = Collections.unmodifiableList(new ArrayList<>(defaultValue));
            return this;
        }

        /**
         * Get a list of permissions.
         *
         * @return The list of permissions
         */
        public List<Permission> getPermissions() {
            return permissions;
        }

        /**
         * Set the list of permissions.
         *
         * @param permissions The list of permissions
         * @return The builder
         */
        public Builder setPermissions(List<Permission> permissions) {
            checkNotNull(permissions, "permissions");
            this.permissions = permissions;
            return this;
        }

        /**
         * Create an instance.
         *
         * <p>Neither {@code name} nor {@code optionType} can be null.</p>
         *
         * @return The instance
         */
        public ImmutableParameter build() {
            checkNotNull(name, "name");
            checkNotNull(optionType, "optionType");
            return new ImmutableParameter(name, optionType, defaultValue, description, permissions);
        }
    }
}
