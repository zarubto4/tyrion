package responses;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class _Response_Interface {

    @JsonIgnore public String message;
    @JsonIgnore public String state;
    @JsonIgnore public Integer code;


    abstract String state();

    abstract Integer code();

    abstract String message();

}
