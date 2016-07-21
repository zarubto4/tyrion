package utilities.swagger.documentationClass;

import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(description = "Json Model for Homer Device List",
        value = "Homer_DeviceList_Result")
public class Swagger_Homer_DeviceList_Result {

      // Komunikujeme přes websocket
      public String  status;

      public List<String> deviceList;

}
