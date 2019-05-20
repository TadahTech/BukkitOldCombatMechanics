package gvlfm78.plugin.OldCombatMechanics.utilities.reflection.sweep;

import gvlfm78.plugin.OldCombatMechanics.utilities.packet.Packet;
import gvlfm78.plugin.OldCombatMechanics.utilities.reflection.Reflector;

public interface SweepPacketDetector {

    /**
     * Returns the instance of a detector that should be compatible with the current server version.
     *
     * @return the {@link SweepPacketDetector} instance to use
     */
    static SweepPacketDetector getInstance() {
        if (Reflector.versionIsNewerOrEqualAs(1, 13, 0)) {
            return new SweepPacketDetectorFrom1_13();
        } else {
            return new SweepPacketDetectorUpTo1_12();
        }
    }

    /**
     * Checks if a packet is a sweep packet.
     *
     * @param packet the packet to check
     * @return true if this is a sweep packet
     */
    boolean isSweepPacket(Packet packet);
}
