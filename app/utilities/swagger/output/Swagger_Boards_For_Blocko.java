package utilities.swagger.output;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.*;
import utilities.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@ApiModel(description = "Json Model for Blocko in Becki for accessible hardware and firmware versions",
        value = "Boards_For_Blocko")
public class Swagger_Boards_For_Blocko {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Logger terminal_logger = new Logger(Swagger_Boards_For_Blocko.class);

/*  VALUES -------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_C_Program_Short_Detail_For_Blocko> c_programs = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_HardwareType> hardware_types = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Model_Hardware> boards = new ArrayList<>();

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_M_Project_Short_Detail_For_Blocko> m_projects = new ArrayList<>();



// C_Programs ----------------------------------------------------------------------------------------------------------

    public void add_C_Programs(List<Model_CProgram> real_c_programs) {

        for (Model_CProgram c_program : real_c_programs) {

            try {
                Swagger_C_Program_Short_Detail_For_Blocko c_program_short_detail_for_blocko = new Swagger_C_Program_Short_Detail_For_Blocko();
                c_program_short_detail_for_blocko.id = c_program.id;
                c_program_short_detail_for_blocko.name = c_program.name;
                c_program_short_detail_for_blocko.description = c_program.description;
                c_program_short_detail_for_blocko.hardware_type_id = c_program.hardware_type_id();

                for (Model_Version version : c_program.getVersions()) {

                    Swagger_C_Program_Versions_Short_Detail_For_Blocko versions_short_detail_for_blocko = new Swagger_C_Program_Versions_Short_Detail_For_Blocko();
                    versions_short_detail_for_blocko.id = version.id;
                    versions_short_detail_for_blocko.version_name = version.name;
                    versions_short_detail_for_blocko.version_description = version.description;

                    if (version.compilation != null) {
                        versions_short_detail_for_blocko.virtual_input_output = version.compilation.virtual_input_output;
                    }

                    c_program_short_detail_for_blocko.versions.add(versions_short_detail_for_blocko);

                }

                c_programs.add(c_program_short_detail_for_blocko);

            } catch (Exception e) {
                terminal_logger.internalServerError(e);
            }
        }
    }

    @ApiModel( value = "C_Program_Short_Detail_For_Blocko")
    class Swagger_C_Program_Short_Detail_For_Blocko {

        @ApiModelProperty(required = true, readOnly = true)
        public UUID id;

        @ApiModelProperty(required = true, readOnly = true)
        public String name;

        @ApiModelProperty(required = true, readOnly = true)
        public String description;

        @ApiModelProperty(required = true, readOnly = true)
        public UUID hardware_type_id;

        @ApiModelProperty(required = true, readOnly = true)
        public List<Swagger_C_Program_Versions_Short_Detail_For_Blocko> versions = new ArrayList<>();
    }

    @ApiModel( value = "C_Program_Versions_Short_Detail_For_Blocko")
    class Swagger_C_Program_Versions_Short_Detail_For_Blocko {

        @ApiModelProperty(required = true, readOnly = true)
        public UUID id;

        @ApiModelProperty(required = true, readOnly = true)
        public String version_name;

        @ApiModelProperty(required = true, readOnly = true)
        public String version_description;

        @ApiModelProperty(required = false, value = "It can be null if server has not image of compilation restored in database", readOnly = true)
        public String virtual_input_output;
    }

// M_Projects ----------------------------------------------------------------------------------------------------------

    public void add_M_Projects(List<Model_MProject> real_m_projects) {

        for (Model_MProject project: real_m_projects) {

            Swagger_M_Project_Short_Detail_For_Blocko m_project_short_detail_for_blocko = new Swagger_M_Project_Short_Detail_For_Blocko();
            m_project_short_detail_for_blocko.id = project.id;
            m_project_short_detail_for_blocko.name = project.name;
            m_project_short_detail_for_blocko.description = project.description;

            for (Model_MProgram program : project.get_m_programs_not_deleted()) {

                Swagger_M_Program_Short_Detail_For_Blocko m_program_short_detail_for_blocko = new Swagger_M_Program_Short_Detail_For_Blocko();
                m_program_short_detail_for_blocko.id = program.id;
                m_program_short_detail_for_blocko.name =program.name;
                m_program_short_detail_for_blocko.description = program.description;

                for (Model_Version version : program.getVersions_not_removed_by_person()) {

                    Swagger_M_Program_Versions_Short_Detail_For_Blocko versions_short_detail_for_blocko = new Swagger_M_Program_Versions_Short_Detail_For_Blocko();
                    versions_short_detail_for_blocko.id = version.id;
                    versions_short_detail_for_blocko.version_name = version.name;
                    versions_short_detail_for_blocko.version_description= version.description;
                    versions_short_detail_for_blocko.virtual_input_output = version.m_program_virtual_input_output;

                    m_program_short_detail_for_blocko.versions.add(versions_short_detail_for_blocko);
                }

                m_project_short_detail_for_blocko.m_programs.add(m_program_short_detail_for_blocko);
            }

            m_projects.add(m_project_short_detail_for_blocko);
        }

    }

    @ApiModel( value = "M_Project_Short_Detail_For_Blocko")
    class Swagger_M_Project_Short_Detail_For_Blocko {

        @ApiModelProperty(required = true, readOnly = true)
        public UUID id;

        @ApiModelProperty(required = true, readOnly = true)
        public String name;

        @ApiModelProperty(required = true, readOnly = true)
        public String description;

        @ApiModelProperty(required = true, readOnly = true)
        public List<Swagger_M_Program_Short_Detail_For_Blocko> m_programs = new ArrayList<>();

    }

    @ApiModel( value = "M_Program_Short_Detail_For_Blocko")
    class Swagger_M_Program_Short_Detail_For_Blocko {

        @ApiModelProperty(required = true, readOnly = true)
        public UUID id;

        @ApiModelProperty(required = true, readOnly = true)
        public String name;

        @ApiModelProperty(required = true, readOnly = true)
        public String description;

        @ApiModelProperty(required = true, readOnly = true)
        public List<Swagger_M_Program_Versions_Short_Detail_For_Blocko> versions = new ArrayList<>();
    }

    @ApiModel( value = "M_Program_Versions_Short_Detail_For_Blocko")
    class Swagger_M_Program_Versions_Short_Detail_For_Blocko {

        @ApiModelProperty(required = true, readOnly = true)
        public UUID id;

        @ApiModelProperty(required = true, readOnly = true)
        public String version_name;

        @ApiModelProperty(required = true, readOnly = true)
        public String version_description;

        @ApiModelProperty(required = true, readOnly = true)
        public String virtual_input_output;

    }

}
