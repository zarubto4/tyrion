package utilities.hardware_updater;

import models.compiler.Model_Board;
import models.compiler.Model_FileRecord;
import models.project.b_program.instnace.Model_HomerInstance;
import utilities.enums.Firmware_type;

import java.util.ArrayList;
import java.util.List;

public class Actualization_Task {

    public Model_HomerInstance instance;
    public String actualization_procedure_id;

    public Model_FileRecord file_record;
    public List<Model_Board> boards = new ArrayList<>();
    public Firmware_type firmware_type;



    public List<String> get_ids(){
        List<String> ids = new ArrayList<>();

        for(Model_Board board : boards){
                ids.add(board.id);
        }

        return ids;
    }
}
