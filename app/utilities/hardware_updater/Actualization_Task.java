package utilities.hardware_updater;

import models.compiler.Board;
import models.compiler.FileRecord;
import models.project.b_program.instnace.Homer_Instance;
import utilities.enums.Firmware_type;
import utilities.webSocket.WebSCType;

import java.util.ArrayList;
import java.util.List;

public class Actualization_Task {

    public Homer_Instance instance;
    public String actualization_procedure_id;

    public FileRecord file_record;
    public List<Board> boards = new ArrayList<>();
    public Firmware_type firmware_type;



    public List<String> get_ids(){
        List<String> ids = new ArrayList<>();

        for(Board board : boards){
                ids.add(board.id);
        }

        return ids;
    }
}
