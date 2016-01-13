package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.blocko.Homer;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import utilities.GlobalResult;
import utilities.Secured;
import webSocket.controllers.SocketCollector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


@Security.Authenticated(Secured.class)
public class MonitoringController  extends Controller {

    public static Result getAllInformation(){
        try {

          List<Homer> allRaspberriesInDatabase = Homer.find.all();
          List<Homer> connectedRaspberries = new ArrayList<>();


          Iterator it = SocketCollector.map.entrySet().iterator();
          List<Homer> connectedDevice = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                connectedDevice.add(Homer.find.byId(pair.getValue().toString()));
            }

            JsonNode rsp = Json.toJson(allRaspberriesInDatabase);
            JsonNode cnr = Json.toJson(connectedRaspberries);

            ObjectNode result = Json.newObject();
            result.put("allRaspberriesInDatabase", rsp);
            result.put("connectedRaspberries", cnr);

            return GlobalResult.okResult( result);

        }catch(Exception e){
            return GlobalResult.badRequestResult(e);
        }
    }




}
