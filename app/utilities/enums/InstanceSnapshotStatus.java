package utilities.enums;

import io.ebean.annotation.EnumValue;

public enum InstanceSnapshotStatus {

    @EnumValue("FUTURE")    FUTURE,
    @EnumValue("NOW")       NOW,
    @EnumValue("HISTORY")   HISTORY;

    public static InstanceSnapshotStatus getType(String value) {

        if (value.toLowerCase().equalsIgnoreCase(FUTURE.toString().toLowerCase()    ))   return InstanceSnapshotStatus.FUTURE;
        if (value.toLowerCase().equalsIgnoreCase(NOW.toString().toLowerCase() ))   return InstanceSnapshotStatus.NOW;
        if (value.toLowerCase().equalsIgnoreCase(HISTORY.toString().toLowerCase() ))   return InstanceSnapshotStatus.HISTORY;
        return null;
    }

}
