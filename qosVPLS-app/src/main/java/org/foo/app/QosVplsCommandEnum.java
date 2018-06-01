package org.foo.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enumerado representando el comando qosVPLS.
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
     * Returns la lista de comandos.
     *
     * @return la lista de strings que se corresponden al enum
     */
    public static List<String> toStringList() {
        return Arrays.stream(values()).map(QosVplsCommandEnum::toString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Devuelve el comando que se corresponde con el String
     *
     * @param comando el string que represanta el comando
     *            
     * @return the EncapsulationType constante que se corresponde con el string
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