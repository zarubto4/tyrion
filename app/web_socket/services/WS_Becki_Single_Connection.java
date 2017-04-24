package web_socket.services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Controller_WebSocket;
import utilities.logger.Class_Logger;

public class WS_Becki_Single_Connection extends WS_Interface_type {

/* LOGGER  -------------------------------------------------------------------------------------------------------------*/

    private static final Class_Logger terminal_logger = new Class_Logger(WS_Becki_Single_Connection.class);

/* VALUES  -------------------------------------------------------------------------------------------------------------*/

    public WS_Becki_Website person_connection;
    public boolean notification_subscriber = false;

    public String identifikator;

    public WS_Becki_Single_Connection(String person_id, WS_Becki_Website person_connection) {
        super();
        this.person_connection = person_connection;
        identifikator = person_id;
        super.webSCtype = this;
    }

    @Override
    public void add_to_map() {
        person_connection.all_person_Connections.put(identifikator, this);
    }

    @Override
    public String get_identificator() {
        return identifikator;
    }

    @Override
    public void onClose() {

        terminal_logger.trace("WS_Becki_Single_Connection::  onClose::  " + identifikator);

        this.close();

        person_connection.all_person_Connections.remove(this.identifikator);

        if(person_connection.all_person_Connections.isEmpty()){
            Controller_WebSocket.becki_website.remove(person_connection.identifikator);
        }

    }


    @Override
    public void onMessage(ObjectNode json) {
        json.put("single_connection_token", this.identifikator);
        person_connection.onMessage(json);
    }
}
