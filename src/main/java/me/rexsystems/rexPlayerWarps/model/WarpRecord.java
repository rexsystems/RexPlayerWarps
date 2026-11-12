package me.rexsystems.rexPlayerWarps.model;

import java.util.Locale;
import java.util.UUID;

public final class WarpRecord {

    private final long id;
    private final String name;
    private final UUID ownerId;
    private final String ownerName;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final String description;
    private final String icon;
    private final String category;
    private final long priceCents;
    private final WarpAccess access;
    private final long balanceCents;
    private final int visits;
    private final long createdAt;

    public WarpRecord(long id, String name, UUID ownerId, String ownerName, String world,
                      double x, double y, double z, float yaw, float pitch, String description,
                      String icon, String category, long priceCents, WarpAccess access,
                      long balanceCents, int visits, long createdAt) {
        this.id = id;
        this.name = name == null ? "" : name;
        this.ownerId = ownerId;
        this.ownerName = ownerName == null ? "" : ownerName;
        this.world = world == null ? "" : world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.description = description == null ? "" : description;
        this.icon = icon == null ? "ENDER_PEARL" : icon;
        this.category = category == null ? "" : category;
        this.priceCents = priceCents;
        this.access = access == null ? WarpAccess.PUBLIC : access;
        this.balanceCents = balanceCents;
        this.visits = visits;
        this.createdAt = createdAt;
    }

    public long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String nameKey() {
        return name.toLowerCase(Locale.ROOT);
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String ownerName() {
        return ownerName;
    }

    public String world() {
        return world;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public String description() {
        return description;
    }

    public String icon() {
        return icon;
    }

    public String category() {
        return category;
    }

    public long priceCents() {
        return priceCents;
    }

    public WarpAccess access() {
        return access;
    }

    public long balanceCents() {
        return balanceCents;
    }

    public int visits() {
        return visits;
    }

    public long createdAt() {
        return createdAt;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private long id;
        private String name;
        private UUID ownerId;
        private String ownerName;
        private String world;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;
        private String description;
        private String icon;
        private String category;
        private long priceCents;
        private WarpAccess access;
        private long balanceCents;
        private int visits;
        private long createdAt;

        public Builder() {
        }

        public Builder(WarpRecord record) {
            this.id = record.id;
            this.name = record.name;
            this.ownerId = record.ownerId;
            this.ownerName = record.ownerName;
            this.world = record.world;
            this.x = record.x;
            this.y = record.y;
            this.z = record.z;
            this.yaw = record.yaw;
            this.pitch = record.pitch;
            this.description = record.description;
            this.icon = record.icon;
            this.category = record.category;
            this.priceCents = record.priceCents;
            this.access = record.access;
            this.balanceCents = record.balanceCents;
            this.visits = record.visits;
            this.createdAt = record.createdAt;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder ownerId(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder ownerName(String ownerName) {
            this.ownerName = ownerName;
            return this;
        }

        public Builder world(String world) {
            this.world = world;
            return this;
        }

        public Builder x(double x) {
            this.x = x;
            return this;
        }

        public Builder y(double y) {
            this.y = y;
            return this;
        }

        public Builder z(double z) {
            this.z = z;
            return this;
        }

        public Builder yaw(float yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder priceCents(long priceCents) {
            this.priceCents = priceCents;
            return this;
        }

        public Builder access(WarpAccess access) {
            this.access = access;
            return this;
        }

        public Builder balanceCents(long balanceCents) {
            this.balanceCents = balanceCents;
            return this;
        }

        public Builder visits(int visits) {
            this.visits = visits;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public WarpRecord build() {
            return new WarpRecord(id, name, ownerId, ownerName, world, x, y, z, yaw, pitch, description, icon,
                    category, priceCents, access, balanceCents, visits, createdAt);
        }
    }
}
