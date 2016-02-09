package models.blocko;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.WebSocketController_Incoming;
import play.libs.Json;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class Homer extends Model {

/* DATABASE VALUES ------------------------------------------------------------------------------------------------------ */
        @Id         public String  homerId;
                    public String  typeOfDevice;
                    public String  version;

    @JsonIgnore @ManyToOne  public Project project;

    @JsonIgnore @OneToMany(mappedBy="homer", cascade = CascadeType.ALL) public List<B_Program_Homer> b_program_clouds = new ArrayList<>();

/* FINDER & WEBSOCKET --------------------------------------------------------------------------------------------------------*/
        public static Finder<String,Homer> find = new Finder<>(Homer.class);

/* METHODS ----------------------------------------------------------------------------------------------------------------*/


        public void aftetConnectionToTyrionServer(){
            // Zkontroluj programy k nahrání
            this.checkUploadPrograms();
        }

        public void checkUploadPrograms(){

        }

        public void sendProgramToHomer(B_Program program, Date when, Date until)throws Exception{

            ObjectNode data = Json.newObject();
            data.put("whenDate", "immediately");
            data.put("whenDate", "immediately");
            data.set("program", Json.toJson(program));

            ObjectNode result = Json.newObject();
            result.put("package", "program");
            result.put("command", "updateProgram");

            result.set("contents", data);

            WebSocketController_Incoming.getConnection(this).write(result.toString());

        }



}



