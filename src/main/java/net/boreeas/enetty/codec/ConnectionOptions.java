package net.boreeas.enetty.codec;

import lombok.Data;

/**
 * @author Malte Schütze
 */
@Data
public class ConnectionOptions {
    private int mtuMax = 4096;
    private int mtuMin = 576;
    private int mtuDefault = 1400;

    private int channelCountMax = 255; // 0xff reserved
    private int channelCountMin = 1;
    private int channelCountDefault = 8;

    private int windowSizeMax = 32768;
    private int windowSizeMin = 4096;
    private int windowSizeDefault = 8192;

    private int peerIdMax = 0x7f;
    private int peerIdMin = 0;

    private int bandwidthUpstreamDefault = 0;
    private int bandwidthDownstreamDefault = 0;
    private int bandwidthThrottleIntervalDefault = 1000;
    private int roundTripTimeDefault = 500;

    private int packetThrottleIntervalDefault = 5000;
    private int packetThrottleValueDefault = 7;
    private int packetThrottleAcceleration = 2;
    private int packetThrottleDeceleration = 2;
}
