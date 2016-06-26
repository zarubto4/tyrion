package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.compiler.Board;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.b_program.Homer;

import java.util.ArrayList;
import java.util.List;

public class B_Program_State {

    public boolean uploaded;
    public String m_project_id;

    @JsonInclude(JsonInclude.Include.NON_NULL) public String version_id;

    @JsonInclude(JsonInclude.Include.NON_NULL) public String where;

    @JsonInclude(JsonInclude.Include.NON_NULL) public B_Program_state_cloud cloud;
    @JsonInclude(JsonInclude.Include.NON_NULL) public B_Program_state_local local;

    public boolean online;
    public List<Board> online_boards = new ArrayList<>();


// --- SET STATE -------------------------------------------------------------------------------------------------------

    public void set_Cloud_State(B_Program_Cloud program_cloud, String server_name, boolean online ){

        cloud = new B_Program_state_cloud();
        cloud.program_cloud = program_cloud;
        cloud.server_name = server_name;
        this.online = online;
    }

    public void set_Local_State(B_Program_Homer program_homer, Homer homer, boolean online ){

        local = new B_Program_state_local();
        local.program_homer = program_homer;
        local.homer = homer;
        this.online = online;
    }

// --- STATE CLASS -----------------------------------------------------------------------------------------------------

    class B_Program_state_cloud{
        public B_Program_Cloud program_cloud;
        public String server_name;

    }


    class B_Program_state_local{
        public B_Program_Homer program_homer;
        public Homer homer;

    }

}


