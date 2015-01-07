package net.boreeas.enet.codec;

import java.io.IOException;

/**
 * @author Malte Schütze
 */
public class OutOfWindowException extends IOException {
    public OutOfWindowException(String s) {
        super(s);
    }
}
