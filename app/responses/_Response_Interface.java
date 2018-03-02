package responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class _Response_Interface {

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
