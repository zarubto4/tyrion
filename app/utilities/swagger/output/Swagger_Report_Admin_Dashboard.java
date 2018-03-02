package utilities.swagger.output;

import io.swagger.annotations.ApiModel;


@ApiModel(value = "Report_Admin_Dashboard")
public class Swagger_Report_Admin_Dashboard {

    public Integer person_registration;
    public Integer project_created;
    public Integer board_registered;

    public Integer homer_server_public_created;
    public Integer homer_server_public_online;

    public Integer homer_server_private_created;
    public Integer homer_server_private_online;

    public Integer compilation_server_public_online;
    public Integer compilation_server_public_created;

    public Integer bugs_reported;

}
