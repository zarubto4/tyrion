package utilities.slack;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultSlackService.class)
public interface SlackService {

    void post(String message);

    void postHomerChannel(String message);

    void postHardwareChannel(String message);
}
