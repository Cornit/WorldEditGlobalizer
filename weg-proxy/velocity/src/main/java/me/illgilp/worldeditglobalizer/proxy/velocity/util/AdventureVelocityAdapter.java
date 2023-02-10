package me.illgilp.worldeditglobalizer.proxy.velocity.util;

import com.velocitypowered.api.proxy.Player;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.UUID;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureVelocityAdapter {

    private static final Class<?> identityClass;
    private static final Class<?> componentClass;
    private static final Class<?> gsonComponentSerializerClass;
    private static final Class<?> messageTypeClass;

    static {
        try {
            identityClass = getClassFromBase64("bmV0Lmt5b3JpLmFkdmVudHVyZS5pZGVudGl0eS5JZGVudGl0eQ==");
            componentClass = getClassFromBase64("bmV0Lmt5b3JpLmFkdmVudHVyZS50ZXh0LkNvbXBvbmVudA==");
            gsonComponentSerializerClass = getClassFromBase64(
                "bmV0Lmt5b3JpLmFkdmVudHVyZS50ZXh0LnNlcmlhbGl6ZXIuZ3Nvbi5Hc29uQ29tcG9uZW50U2VyaWFsaXplcg==");
            messageTypeClass = getClassFromBase64("bmV0Lmt5b3JpLmFkdmVudHVyZS5hdWRpZW5jZS5NZXNzYWdlVHlwZQ==");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(Player player, Identity identity, Component message, MessageType messageType) {
        Object adaptedIdentity;
        try {
            adaptedIdentity = identityClass.getMethod("identity", UUID.class)
                .invoke(null, identity.uuid());
        } catch (Exception e) {
            throw new RuntimeException("Failed to adapt identity", e);
        }
        Object adaptedComponent = getAdaptedComponent(message);
        Object adaptedMessageType = null;
        try {
            for (Object enumConstant : messageTypeClass.getEnumConstants()) {
                String name = (String) enumConstant.getClass().getMethod("name").invoke(enumConstant);
                if (name.equals(messageType.name())) {
                    adaptedMessageType = enumConstant;
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to adapt message type", e);
        }
        if (adaptedMessageType == null) {
            throw new RuntimeException("Could not find adaptable message type '" + messageType.name() + "'");
        }
        try {
            player.getClass()
                .getMethod("sendMessage", identityClass, componentClass, messageTypeClass)
                .invoke(player, adaptedIdentity, adaptedComponent, adaptedMessageType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke sendMessage on velocity player", e);
        }
    }

    public static void sendActionBar(Player player, Component message) {
        Object adaptedComponent = getAdaptedComponent(message);
        try {
            player.getClass().getMethod("sendActionBar", componentClass)
                .invoke(player, adaptedComponent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke sendActionBar on velocity player", e);
        }
    }

    private static Object getAdaptedComponent(Component message) {
        Object adaptedComponent;
        try {
            adaptedComponent = gsonComponentSerializerClass.getMethod("deserialize", Object.class)
                .invoke(
                    gsonComponentSerializerClass.getMethod("gson").invoke(null),
                    GsonComponentSerializer.gson()
                        .serialize(message)
                );
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to adapt component", e);
        }
        return adaptedComponent;
    }

    private static Class<?> getClassFromBase64(String base64EncodedClassName) throws ClassNotFoundException {
        return Class.forName(new String(Base64.getDecoder().decode(base64EncodedClassName)));
    }
}


