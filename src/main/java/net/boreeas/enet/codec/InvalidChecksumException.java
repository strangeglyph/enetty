package net.boreeas.enet.codec;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.boreeas.enet.commands.ENetCommand;

/**
 * Created by malte on 12/18/14.
 */
@Getter
@AllArgsConstructor
public class InvalidChecksumException extends Exception {

    private int checksum;
    private int sessionId;
    private ENetCommand command;
}
