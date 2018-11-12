package utilities.hardware.update;

import utilities.enums.FirmwareType;

import java.util.UUID;

public interface Updatable {

    FirmwareType getComponentType();

    UUID getId();
}
