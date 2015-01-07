package net.boreeas.enetty.commands;

import io.netty.buffer.ByteBuf;

/**
 * @author Malte Schütze
 */
public interface DataCommand {
    ByteBuf getData();
}
