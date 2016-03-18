package models.persons;

import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class LinkedAccount extends Model {

	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
									     					 public String providerUserId;
						@Column(columnDefinition = "TEXT")   public String providerKey;
										 					 public String typeOfConnection;
															 public String returnUrl;
															 public Date dateOfCreate;

									@JsonIgnore	@ManyToOne   public Person person;
															 public String user_agent;
															 public String authToken;
															 public boolean tokenVerified;

	@JsonIgnore
	public static LinkedAccount setProviderKey(String typeOfConnection ){
		LinkedAccount linkedAccount = new LinkedAccount();
		while(true){ // I need Unique Value
			String key = UUID.randomUUID().toString();
			if (LinkedAccount.find.where().eq("providerKey",key).findUnique() == null) {
				linkedAccount.providerKey = key;
				break;
			}
		}

		while(true){ // I need Unique Value
			String authToken = UUID.randomUUID().toString();
			if (LinkedAccount.find.where().eq("authToken",authToken).findUnique() == null) {
				linkedAccount.authToken = authToken;
				break;

			}
		}

		linkedAccount.typeOfConnection = typeOfConnection;
        linkedAccount.dateOfCreate = new Date();
		linkedAccount.save();
		return linkedAccount;
	}


	//#### SECURITY LOGIN ##################################################################################################
	public static final Finder<String, LinkedAccount> find = new Finder<>(LinkedAccount.class);

}