package me.illgilp.worldeditglobalizerbungee.intake.parametric.provider;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;
import me.illgilp.intake.argument.ArgumentException;
import me.illgilp.intake.argument.ArgumentParseException;
import me.illgilp.intake.argument.CommandArgs;
import me.illgilp.intake.parametric.Provider;
import me.illgilp.intake.parametric.ProvisionException;
import me.illgilp.intake.parametric.annotation.Validate;
import me.illgilp.worldeditglobalizerbungee.manager.SchematicManager;

public class SavedSchematicArgProvider implements Provider<String> {


    @Override
    public boolean isProvided() {
        return true;
    }

    @Override
    public String get(CommandArgs arguments, List<? extends Annotation> modifiers) throws ArgumentException, ProvisionException {
        String v = arguments.next();
        validate(v, modifiers);
        return v;
    }

    @Override
    public List<String> getSuggestions(String prefix) {
        return SchematicManager.getInstance().getSchematics().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
    }

    private void validate(String string, List<? extends Annotation> modifiers) throws ArgumentParseException {
        if (string == null) {
            return;
        }

        for (Annotation modifier : modifiers) {
            if (modifier instanceof Validate) {
                Validate validate = (Validate) modifier;

                if (!validate.regex().isEmpty()) {
                    if (!string.matches(validate.regex())) {
                        throw new ArgumentParseException(
                            String.format(
                                "The given text doesn't match the right format (technically speaking, the 'format' is %s)",
                                validate.regex()));
                    }
                }
            }
        }
    }
}
