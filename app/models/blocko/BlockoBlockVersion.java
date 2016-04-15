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

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date date_of_create;

                         @Column(columnDefinition = "TEXT")     public String design_json;
                         @Column(columnDefinition = "TEXT")     public String logic_json;
                                     @JsonIgnore @ManyToOne     public BlockoBlock blocko_block;



/* PERMISSION ----------------------------------------------------------------------------------------------------------*/

    @JsonProperty public Boolean create_permission()  {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||   SecurityController.getPerson().has_permission("BlockoBlock.create"); }
    @JsonProperty public Boolean read_permission()    {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||   SecurityController.getPerson().has_permission("BlockoBlock.read"); }
    @JsonProperty public Boolean edit_permission()    {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||   SecurityController.getPerson().has_permission("BlockoBlock.edit");  }
    @JsonProperty public Boolean delete_permission()  {  return  ( BlockoBlockVersion.find.where().eq("blocko_block.author.id", SecurityController.getPerson().id).where().eq("id", id).findRowCount() > 0) ||   SecurityController.getPerson().has_permission("BlockoBlock.delete");}


/* FINDER -------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,BlockoBlockVersion> find = new Finder<>(BlockoBlockVersion.class);

}
