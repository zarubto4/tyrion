package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import controllers.SecurityController;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class BlockoBlockVersion extends Model {

 /* DATABASE VALUE  ----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String version_name;
                                                                public String version_description;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1461918607") public Date date_of_create;

                         @Column(columnDefinition = "TEXT")     public String design_json;
                         @Column(columnDefinition = "TEXT")     public String logic_json;
                                     @JsonIgnore @ManyToOne     public BlockoBlock blocko_block;



/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty @Transient public Boolean create_permission()  {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||  SecurityController.getPerson().has_permission("BlockoBlock_create"); }
    @JsonProperty @Transient public Boolean read_permission()    {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||  SecurityController.getPerson().has_permission("BlockoBlock_read");   }
    @JsonProperty @Transient public Boolean edit_permission()    {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||  SecurityController.getPerson().has_permission("BlockoBlock_edit");   }
    @JsonProperty @Transient public Boolean delete_permission()  {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||  SecurityController.getPerson().has_permission("BlockoBlock_delete"); }

    public enum permissions{BlockoBlock_create, BlockoBlock_read, BlockoBlock_edit, BlockoBlock_delete}

/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,BlockoBlockVersion> find = new Finder<>(BlockoBlockVersion.class);

}
