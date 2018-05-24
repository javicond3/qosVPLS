package org.foo.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum representing the VPLS command type.
 */
public enum QosVplsCommandEnum {
    
    ADD_QOS("add-qos"), DELETE_QOS("delete-qos"), SHOW("show"), CLEAN("clean");
    private final String command;
    
    /**
     * Creates the enum from a string representing the command.
     *
     * @param command
     *            the text representing the command
     */
    QosVplsCommandEnum(final String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }

    /**
     * Returns a list of command string values.
     *
     * @return the list of string values corresponding to the enums
     */
    public static List<String> toStringList() {
        return Arrays.stream(values()).map(QosVplsCommandEnum::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Alternative method to valueOf. It returns the command type corresponding
     * to the given string. If the parameter does not match a constant name, or
     * is null, null is returned.
     *
     * @param command
     *            the string representing the encapsulation type
     * @return the EncapsulationType constant corresponding to the string given
     */
    public static QosVplsCommandEnum enumFromString(String command) {
        if (command != null && !command.isEmpty()) {
            for (QosVplsCommandEnum c : values()) {
                if (command.equalsIgnoreCase(c.toString())) {
                    return c;
                }
            }
        }
        return null;
    }
}