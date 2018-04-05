package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;


@ApiModel(value = "Report_Admin_Dashboard")
public class Swagger_Report_Admin_Dashboard extends _Swagger_Abstract_Default {

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
