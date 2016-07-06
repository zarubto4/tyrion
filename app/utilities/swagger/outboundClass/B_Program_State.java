package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import models.compiler.Board;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;

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

    public void set_Cloud_State(Homer_Instance instance, Cloud_Homer_Server server, boolean online ){

        cloud = new B_Program_state_cloud();
        cloud.instance = instance;
        cloud.server_name = server.server_name;
        this.online = online;
    }

    public void set_Local_State(Homer_Instance instance, Private_Homer_Server server, boolean online ){

        local = new B_Program_state_local();
        local.instance = instance;
        local.server = server;
        this.online = online;
    }

// --- STATE CLASS -----------------------------------------------------------------------------------------------------

    class B_Program_state_cloud{
        public Homer_Instance instance;
        public String server_name;

    }


    class B_Program_state_local{
        public Homer_Instance instance;
        public Private_Homer_Server server;
    }

}


