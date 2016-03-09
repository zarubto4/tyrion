package models.project.m_program;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.grid.Screen_Size_Type;
import utilities.Server;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class M_Program extends Model{

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                                public String program_name;
                            @Column(columnDefinition = "TEXT")  public String program_description;
                @JsonIgnore @Column(columnDefinition = "TEXT")  public String programInString;

                                                                public boolean height_lock;
                                                                public boolean width_lock;

                                                                public Date date_of_create;
                                                                public Date last_update;
                                                                public String qr_token;

                                    @JsonIgnore @ManyToOne      public M_Project m_project_object; // TODO přejmenovat zpět
                                    @JsonIgnore @ManyToOne      public Screen_Size_Type screen_size_type_object;

               @JsonProperty public String program()               {  return Server.serverAddress + "/grid/m_project/program/" + id;}
    @Transient @JsonProperty public String m_project()             {  return Server.serverAddress + "/grid/m_project/" + m_project_object.id; }
    @Transient @JsonProperty public String screen_size_type()      {  return Server.serverAddress + "/grid/screen_type/" + screen_size_type_object.id; }


    //***** Private ****************************************************************************************************

    @JsonIgnore
    public void set_QR_Token() {
        while(true){ // I need Unique Value
            this.qr_token  = UUID.randomUUID().toString();
            if (M_Program.find.where().eq("qr_token", this.qr_token ).findUnique() == null) break;
        }
    }

    public static Finder<String,M_Program> find = new Finder<>(M_Program.class);


}
