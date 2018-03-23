package responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import utilities.swagger.output.filter_results._Swagger_Abstract_Default;

public abstract class _Response_Interface extends _Swagger_Abstract_Default {

    @JsonIgnore public String message;
    @JsonIgnore public String state;
    @JsonIgnore public Integer code;

    @JsonProperty
    abstract String state();

    @JsonProperty
    abstract int code();

    @JsonProperty
    abstract String message();

}
