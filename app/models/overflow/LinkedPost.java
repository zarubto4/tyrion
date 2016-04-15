package models.overflow;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import models.person.Person;

import javax.persistence.*;

@Entity
public class LinkedPost extends Model {

/* DATABASE VALUE  -----------------------------------------------------------------------------------------------------*/

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String linkId;

    @JsonIgnore  @ManyToOne public Person author;
                 @ManyToOne public Post answer;  // Objekt který je odpovědí
    @JsonIgnore  @ManyToOne public Post question; // Objekt na který je navěšuje již odpovězená (podobná) otázka


/* JSON IGNORE ---------------------------------------------------------------------------------------------------------*/


/* FINDER --------------------------------------------------------------------------------------------------------------*/
    public static Finder<String,LinkedPost> find = new Finder<>(LinkedPost.class);



}
