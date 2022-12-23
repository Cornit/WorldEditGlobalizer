package me.illgilp.worldeditglobalizer.common.network;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
class IncomingFragmentedPacketData {

    private final List<DataFrame> frames = new ArrayList<>();
    private boolean complete;


    public void appendFrame(DataFrame frame) {
        if (this.complete) {
            throw new IllegalStateException("IncomingFragmentedPacketData already complete");
        }
        this.frames.add(frame);
        if (!frame.isMoreFollowing()) {
            this.complete = true;
        }
    }

    public boolean isComplete() {
        return complete;
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
