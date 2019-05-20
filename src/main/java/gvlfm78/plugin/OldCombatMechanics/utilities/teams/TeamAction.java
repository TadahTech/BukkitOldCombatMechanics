package gvlfm78.plugin.OldCombatMechanics.utilities.teams;

/**
 * The different actions a {@code PacketPlayOutScoreboardTeam} packet can represent.
 */
public enum TeamAction {
    CREATE(0),
    DISBAND(1),
    UPDATE(2),
    ADD_PLAYER(3),
    REMOVE_PLAYER(4);

    private int minecraftId;

    TeamAction(int minecraftId) {
        this.minecraftId = minecraftId;
    }

    public static TeamAction fromId(int id) {
        for (TeamAction rule : values()) {
            if (rule.getMinecraftId() == id) {
                return rule;
            }
        }
        return null;
    }

    public int getMinecraftId() {
        return minecraftId;
    }
}
