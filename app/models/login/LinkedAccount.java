package models.login;


import com.avaje.ebean.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.math.BigInteger;
import java.security.SecureRandom;

@Entity
public class LinkedAccount extends Model {

	@Id @GeneratedValue(strategy = GenerationType.SEQUENCE)  public String id;
									     					 public String providerUserId;
						@Column(columnDefinition = "TEXT")   public String providerKey;
										 					 public String typeOfConnection;

									@JsonIgnore	@ManyToOne   public Person person;
															 public String authToken;
															 public boolean tokenVerified;

	public static final Finder<String, LinkedAccount> find = new Finder<>(LinkedAccount.class);


	@JsonIgnore
	public static LinkedAccount setProviderKey(String typeOfConnection ){
		LinkedAccount linkedAccount = new LinkedAccount();
		while(true){ // I need Unique Value
			String key = new BigInteger(130, new SecureRandom()).toString(32).toLowerCase();
			if (LinkedAccount.find.where().eq("providerKey",key).findUnique() == null) {
				linkedAccount.providerKey = key;
				break;
			}
		}

		while(true){ // I need Unique Value
			String authToken = new BigInteger(130, new SecureRandom()).toString(32).toLowerCase();
			if (LinkedAccount.find.where().eq("authToken",authToken).findUnique() == null) {
				linkedAccount.authToken = authToken;
				break;
			}
		}

		linkedAccount.typeOfConnection = typeOfConnection;
		linkedAccount.save();
		return linkedAccount;
	}

	//#### SECURITY LOGIN ##################################################################################################


	// If userDB/system make log out
	public void deleteAuthToken() {
		authToken = null;
		save();
	}


}