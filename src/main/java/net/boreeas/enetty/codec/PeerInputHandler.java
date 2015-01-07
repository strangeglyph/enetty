package net.boreeas.enetty.codec;

import io.netty.buffer.ByteBuf;
import lombok.Setter;
import net.boreeas.enetty.commands.DataCommand;
import net.boreeas.enetty.commands.ENetCommand;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Malte Schütze
 */
public class PeerInputHandler {
    @Setter private Consumer<ENetCommand> onUnhandledInput;
    // Maybe a composite key would be a nice choice, but this way, we can use
    // an enum map for send type demuxing.
    private Map<ENetCommand.SendType, Map<Integer, Consumer<ByteBuf>>> handlers = new EnumMap<>(ENetCommand.SendType.class);

    { // Ensure the outer map is fully initialized
        for (ENetCommand.SendType sendType : ENetCommand.SendType.values()) {
            handlers.put(sendType, new HashMap<>());
        }
    }

    public void setHandler(ENetCommand.SendType sendType, int channelId, Consumer<ByteBuf> handler) {
        handlers.get(sendType).put(channelId, handler);
    }

    public void removeHandler(ENetCommand.SendType sendType, int channelId) {
        handlers.get(sendType).remove(channelId);
    }

    public void dispatch(ENetCommand command) {

        if (!(command instanceof DataCommand)) return;
        DataCommand asDataCmd = (DataCommand) command;

        Map<Integer, Consumer<ByteBuf>> lookup = handlers.get(command.getSendType());
        if (lookup.containsKey(command.getChannelId())) {
            lookup.get(command.getChannelId()).accept(asDataCmd.getData());
        } else if (onUnhandledInput != null) {
            onUnhandledInput.accept(command);
        }
    }
}
