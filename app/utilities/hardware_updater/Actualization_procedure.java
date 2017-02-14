package utilities.hardware_updater;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.compiler.Model_Board;
import models.compiler.Model_FileRecord;
import utilities.enums.Firmware_type;

import java.util.ArrayList;
import java.util.List;

public class Actualization_procedure {

    @JsonIgnore
    public List<Model_Board> boards = new ArrayList<>();
    @JsonIgnore   public Model_FileRecord file_record;


    @JsonProperty
    public Firmware_type firmwareType;
    @JsonProperty  public  String actualizationProcedureId;

    @JsonProperty public List<String> targetIds(){
        List<String> ids = new ArrayList<>();
        for(Model_Board board : boards)  ids.add(board.id);
        return ids;
    }

    @JsonProperty
    public String program(){
        return file_record.get_fileRecord_from_Azure_inString();
    }

    @JsonProperty
    public String buildId(){
        if(firmwareType == Firmware_type.FIRMWARE)    return  file_record.c_compilations_binary_file.firmware_build_id;
        if(firmwareType == Firmware_type.BACKUP)      return  file_record.c_compilations_binary_file.firmware_build_id; // Tady je chyba
        if(firmwareType == Firmware_type.BOOTLOADER)  return  file_record.boot_loader.version_identificator;
        else return null;
    }
}