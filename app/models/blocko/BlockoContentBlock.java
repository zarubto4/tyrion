package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

@Entity
public class BlockoContentBlock extends Model {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String versionName;
                                                                public String versionDescription;
                                                                public Date   dateOfCreate;
                         @Column(columnDefinition = "TEXT")     public String designJson;
                         @Column(columnDefinition = "TEXT")     public String logicJson;
                                     @JsonIgnore @ManyToOne     public BlockoBlock blockoBlock;


    // JsonIgnore Methods **********************************************************************************************



    // Finder **********************************************************************************************************
    public static Finder<String,BlockoContentBlock> find = new Finder<>(BlockoContentBlock.class);
}
