package utilities.hardware_updater.helps_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Model_FileRecord;
import utilities.enums.Enum_Firmware_type;
import utilities.enums.Enum_Update_type_of_update;

import java.util.ArrayList;
import java.util.List;

public class Utilities_HW_Updater_Actualization_procedure {

    /**
     *   In this Class is Code Standard by HOMER - Please Visit Wiki for that!!!!
     */



    @JsonProperty public List<Utilities_HW_Updater_Target_pair> targetPairs = new ArrayList<>();

    @JsonIgnore   public Model_FileRecord file_record;
    @JsonProperty public String name;
    @JsonProperty public String version;


    @JsonProperty public Enum_Firmware_type firmwareType;
    @JsonProperty public Enum_Update_type_of_update typeOfUpdate;
    @JsonProperty public  String actualizationProcedureId;

    @JsonProperty
    public String program(){
        return file_record.get_fileRecord_from_Azure_inString();
    }



    @JsonProperty
    public String buildId(){
        if(firmwareType == Enum_Firmware_type.FIRMWARE)         return  file_record.c_compilations_binary_file.firmware_build_id;
        else if(firmwareType == Enum_Firmware_type.BACKUP)      return  file_record.c_compilations_binary_file.firmware_build_id; // Tady je chyba
        else if(firmwareType == Enum_Firmware_type.BOOTLOADER)  return  file_record.boot_loader.version_identificator;
        else return null;
    }

}