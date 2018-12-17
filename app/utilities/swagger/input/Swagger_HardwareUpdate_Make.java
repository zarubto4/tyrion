package utilities.swagger.input;

import exceptions.NotFoundException;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.*;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import utilities.enums.CompilationStatus;
import utilities.enums.FirmwareType;
import utilities.hardware.update.Updatable;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@ApiModel(description = "Json Model for ActualizationProcedure Make Procedure",
          value = "HardwareUpdate_Make")
public class Swagger_HardwareUpdate_Make extends Swagger_NameAndDescription implements Constraints.Validatable<List<ValidationError>>  {


    @Constraints.Required @ApiModelProperty(required = true,  readOnly = true) public UUID project_id;
    @Constraints.Required @ApiModelProperty(required = true,  readOnly = true) public FirmwareType firmware_type;

    @ApiModelProperty(required = false, readOnly = true)  public List<UUID> hardware_group_ids;
    @ApiModelProperty(required = false, readOnly = true)  public List<UUID> hardware_ids;

    @Valid
    @ApiModelProperty(required = true,  readOnly = true) public List<Swagger_HardwareUpdate_Make_HardwareType> hardware_type_settings = new ArrayList<>();

    @ApiModelProperty(required = false,  readOnly = true, value = "If  value is null - its a command for immediately update ") public Long time;
    @ApiModelProperty(required = false,  readOnly = true, value = "If  value is null - its a command for immediately update - Default Value 0") public Integer timeoffset = 0;


    @Override
    public List<ValidationError> validate() {

        List<ValidationError> errors = new ArrayList<>();

        for(Swagger_HardwareUpdate_Make_HardwareType type : hardware_type_settings) {


            if (type.c_program_version_id == null && type.bootloader_id == null) {
                errors.add(new ValidationError("hardware_type_settings.c_program_version_id", "You have to set c_program_version_id or bootloader_id"));
                errors.add(new ValidationError("hardware_type_settings.bootloader_id", "You have to set c_program_version_id or bootloader_id"));
                continue;
            }

            if ( firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
                if ( Model_CProgramVersion.find.byId(type.c_program_version_id).status() != CompilationStatus.SUCCESS) {
                    errors.add(new ValidationError("hardware_type_settings.c_program_version_id","C Program Must be successfully compiled!"));
                }

            }

            if (firmware_type == FirmwareType.BOOTLOADER) {
                Model_BootLoader bootLoader = Model_BootLoader.find.byId(type.bootloader_id);
                if (!bootLoader.getHardwareTypeId().equals(type.hardware_type_id)) {
                    errors.add(new ValidationError("hardware_type_settings.bootloader", "Invalid type of Bootloader for HardwareType"));
                }
            }
        }


        if (time != null && time != 0L) {
            try {
                Date date_of_planing = new Date(time);
                if (date_of_planing.getTime() < (new Date().getTime() - 5000)) {
                    errors.add(new ValidationError("time", "Invalid Time Format - Past time is not legal"));
                }
            } catch (Exception e) {
                errors.add(new ValidationError("time", "Invalid Time Format"));
            }
        }

        if((hardware_group_ids == null || hardware_group_ids.isEmpty())  && (hardware_ids == null || hardware_ids.isEmpty())) {
            errors.add(new ValidationError("hardware_group_ids", "hardware_ids or hardware_group_ids must contains at least one ID"));
        }


        return errors.isEmpty() ? null : errors;

    }

    public Updatable getComponent(Model_HardwareType hardware_type) {

        if ( firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
            for(Swagger_HardwareUpdate_Make_HardwareType type : hardware_type_settings) {

                if(type.hardware_type_id.equals(hardware_type.getId())) {
                    return Model_CProgramVersion.find.byId(type.c_program_version_id);
                }
            }
        }

        if (firmware_type == FirmwareType.BOOTLOADER) {
            for(Swagger_HardwareUpdate_Make_HardwareType type : hardware_type_settings) {
                if(type.hardware_type_id.equals(hardware_type.getId())) {
                    return Model_BootLoader.find.byId(type.bootloader_id);
                }
            }
        }

        if ( firmware_type == FirmwareType.FIRMWARE || firmware_type == FirmwareType.BACKUP) {
            throw new NotFoundException(Model_CProgramVersion.class, "Not find Component");
        } else {
            throw new NotFoundException(Model_BootLoader.class, "Not find Component");
        }


    }
}
