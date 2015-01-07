package net.boreeas.enetty.commands;

import lombok.AllArgsConstructor;

/**
 * @author Malte Schütze
 */
@AllArgsConstructor
public enum CommandId {
    ACKNOWLEDGE(1),
    CONNECT(2),
    VERIFY_CONNECT(3),
    DISCONNECT(4),
    PING(5),
    SEND_RELIABLE(6),
    SEND_UNRELIABLE(7),
    SEND_FRAGMENT(8),
    SEND_UNSEQUENCED(9),
    BANDWIDTH_LIMIT(10),
    THROTTLE_CONFIGURE(11);

    public final int id;

    public static CommandId getById(int id) {
        return values()[id - 1];
    }
}
