package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.project.b_program.B_Program_Cloud;
import models.project.b_program.B_Program_Homer;
import models.project.b_program.Homer;

public class B_Program_State {

    public boolean uploaded;
    public String m_project_id;

    @JsonInclude(JsonInclude.Include.NON_NULL) public String version_id;

    @JsonInclude(JsonInclude.Include.NON_NULL) public String where;

    @JsonInclude(JsonInclude.Include.NON_NULL) public B_Program_state_cloud cloud;
    @JsonInclude(JsonInclude.Include.NON_NULL) public B_Program_state_local local;


// --- SET STATE -------------------------------------------------------------------------------------------------------

    public void set_Cloud_State(B_Program_Cloud program_cloud, String server_name ){

        cloud = new B_Program_state_cloud();
        cloud.program_cloud = program_cloud;
        cloud.server_name = server_name;


    }

    public void set_Local_State(B_Program_Homer program_homer, Homer homer ){

        local = new B_Program_state_local();
        local.program_homer = program_homer;
        local.homer = homer;

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


