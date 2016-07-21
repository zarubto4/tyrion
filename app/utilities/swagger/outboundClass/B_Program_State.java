package utilities.swagger.outboundClass;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.compiler.Board;
import models.project.b_program.Homer_Instance;
import models.project.b_program.servers.Cloud_Homer_Server;
import models.project.b_program.servers.Private_Homer_Server;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Json Model for B_Program state",
        value = "B_Program_State")
public class B_Program_State {

    @ApiModelProperty(required = true, readOnly = true)
    public boolean uploaded;

    @ApiModelProperty(required = true, readOnly = true)
    public String m_project_id;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String version_id;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String where;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public B_Program_state_cloud cloud;

    @ApiModelProperty(required = false, readOnly = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public B_Program_state_local local;

    @ApiModelProperty(required = true, readOnly = true)
    public boolean online;

    @ApiModelProperty(required = true, readOnly = true)
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


