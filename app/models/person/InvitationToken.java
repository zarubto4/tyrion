package models.person;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.project.global.Project;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class InvitationToken extends Model{

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)     public String id;
                                                    @ManyToOne  public Person owner;
                                                    @ManyToOne  public Project project;
                                                                public String mail;
                                                                public String invitation_token;
                                                                public Date time_of_creation;

/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/

    @JsonIgnore @Transient
    public void  setInvitationToken(){
        while(true){ // I need Unique Value
            this.invitation_token = UUID.randomUUID().toString();
            if (InvitationToken.find.where().eq("invitation_token",this.invitation_token).findUnique() == null) break;
        }
    }

/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,InvitationToken> find = new Finder<>(InvitationToken.class);

}
