package net.boreeas.enetty.codec;

import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * @author Malte Schütze
 */
@NoArgsConstructor
public class MtuExceededException extends IOException {
    public MtuExceededException(String msg) {
        super(msg);
    }

    public MtuExceededException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public MtuExceededException(Throwable cause) {
        super(cause);
    }
}
