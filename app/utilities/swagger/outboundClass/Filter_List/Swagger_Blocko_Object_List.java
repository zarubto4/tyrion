package utilities.swagger.outboundClass.Filter_List;

import com.avaje.ebean.Query;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import models.blocko.BlockoBlock;
import models.blocko.TypeOfBlock;
import models.project.b_program.B_Program;
import utilities.swagger.outboundClass.Swagger_B_Program_Light;
import utilities.swagger.outboundClass.Swagger_Blocko_Block_Light;
import utilities.swagger.outboundClass.Swagger_Type_Of_Block_Light;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "Individual Blocko Object List",
        value = "Blocko_Object_List")
public class Swagger_Blocko_Object_List {


/* Content--------------------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_B_Program_Light> content_b_program;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Type_Of_Block_Light> content_type_of_block;

    @ApiModelProperty(required = true, readOnly = true)
    public List<Swagger_Blocko_Block_Light> content_blocko_block;

/* Basic Filter Value --------------------------------------------------------------------------------------------------*/

    @ApiModelProperty(required = true, readOnly = true, value = "First value position from all subjects. Minimum is 0.")
    public int from;

    @ApiModelProperty(required = true, readOnly = true, value = "Minimum is \"from\" Maximum is \"total\"")
    public int to;

    @ApiModelProperty(required = true, readOnly = true, value = "Total subjects")
    public int total;

    @ApiModelProperty(required = true, readOnly = true, value = "Numbers of pages, which you can call")
    public List<Integer> pages = new ArrayList<>();

/* Set -----------------------------------------------------------------------------------------------------------------*/

    public Swagger_Blocko_Object_List(Query<B_Program> query_b_program , Query<TypeOfBlock> query_type_of_block , Query<BlockoBlock> query_blocko_block , int page_number){

        if(page_number < 1) page_number = 1;

        List<B_Program> b_programs =  query_b_program.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        for(B_Program b_program : b_programs){

            Swagger_B_Program_Light help = new Swagger_B_Program_Light();

            help.b_program_id = b_program.id;
            help.b_program_name = b_program.name;
            help.b_program_version_id = b_program.version_objects.get(0).id;
            help.b_program_version_name = b_program.version_objects.get(0).version_name;

            this.content_b_program.add(help);
        }

        List<TypeOfBlock> typeOfBlocks =  query_type_of_block.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        for(TypeOfBlock typeOfBlock : typeOfBlocks){

            Swagger_Type_Of_Block_Light help = new Swagger_Type_Of_Block_Light();

            help.type_of_block_id = typeOfBlock.id;
            help.type_of_block_name = typeOfBlock.name;

            this.content_type_of_block.add(help);
        }

        List<BlockoBlock> blockoBlocks =  query_blocko_block.setFirstRow((page_number - 1) * 25).setMaxRows(25).findList();
        for(BlockoBlock blockoBlock : blockoBlocks){

            Swagger_Blocko_Block_Light help = new Swagger_Blocko_Block_Light();

            help.blocko_block_id = blockoBlock.id;
            help.blocko_block_name = blockoBlock.name;
            help.blocko_block_version_id = blockoBlock.blocko_versions.get(0).id;
            help.blocko_block_version_name = blockoBlock.blocko_versions.get(0).version_name;

            this.content_blocko_block.add(help);
        }

        this.total   = query.findRowCount();
        this.from   = (page_number - 1) * 25;
        this.to     = (page_number - 1) * 25 + content.size();
        for (int i = 1; i < (total / 25) + 2; i++) pages.add(i);
    }
}
