package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.*;
import java.util.Date;

@Entity
public class BlockoBlockVersion extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String version_name;
                                                                public String version_description;

    @ApiModelProperty(required = true, dataType = "integer", readOnly = true, value = "UNIX time stamp", example = "1458315085338") public Date   dateOfCreate;

                         @Column(columnDefinition = "TEXT")     public String design_json;
                         @Column(columnDefinition = "TEXT")     public String logic_json;
                                     @JsonIgnore @ManyToOne     public BlockoBlock blocko_block;


    // JsonIgnore Methods **********************************************************************************************



    // Finder **********************************************************************************************************
    public static Finder<String,BlockoBlockVersion> find = new Finder<>(BlockoBlockVersion.class);
}
