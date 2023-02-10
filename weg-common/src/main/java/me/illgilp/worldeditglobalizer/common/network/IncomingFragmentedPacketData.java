package me.illgilp.worldeditglobalizer.common.network;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
class IncomingFragmentedPacketData {

    private final List<DataFrame> frames = new ArrayList<>();
    @Getter
    private boolean complete;
    @Getter
    private boolean cancelled;


    public void appendFrame(DataFrame frame) {
        if (this.complete) {
            throw new IllegalStateException("IncomingFragmentedPacketData already complete");
        }
        if (this.cancelled) {
            throw new IllegalStateException("IncomingFragmentedPacketData already cancelled");
        }
        this.frames.add(frame);
        if (!frame.isMoreFollowing()) {
            this.complete = true;
        }
        if (frame.isCancelled()) {
            this.cancelled = true;
        }
    }

    public byte[] getPacketData() {
        final byte[] data = new byte[this.frames.stream().mapToInt(frame -> frame.getPayload().length).sum()];
        int pos = 0;
        for (DataFrame frame : this.frames) {
            final byte[] payload = frame.getPayload();
            System.arraycopy(payload, 0, data, pos, payload.length);
            pos += payload.length;
        }
        return data;
    }
}
