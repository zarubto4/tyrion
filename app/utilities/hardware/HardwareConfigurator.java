package utilities.hardware;

import exceptions.NotSupportedException;
import models.Model_Hardware;
import utilities.document_mongo_db.document_objects.DM_Board_Bootloader_DefaultConfig;
import utilities.logger.Logger;
import utilities.swagger.input.Swagger_Board_Developer_parameters;
import websocket.Message;
import websocket.messages.homer_hardware_with_tyrion.WS_Message_Hardware_overview_Board;

import java.lang.reflect.Field;
import java.util.concurrent.CompletionStage;

public class HardwareConfigurator {

    private static final Logger logger = new Logger(HardwareConfigurator.class);

    private final Model_Hardware hardware;
    private final HardwareInterface hardwareInterface;

    private DM_Board_Bootloader_DefaultConfig configuration;

    public HardwareConfigurator(Model_Hardware hardware, HardwareInterface hardwareInterface) {
        this.hardware = hardware;
        this.hardwareInterface = hardwareInterface;

        this.configuration = hardware.bootloader_core_configuration();
    }

    /**
     * Configures all settings in hardware.
     */
    public boolean configure(WS_Message_Hardware_overview_Board overview) {
        boolean changeSettings = false; // Pokud došlo ke změně
        boolean changeConfig = false;

        try {

            for (Field configField : configuration.getClass().getFields()) {

                String configFieldName = configField.getName();
                Field reportedField;

                try {
                    reportedField = overview.getClass().getField(configFieldName);
                } catch (NoSuchFieldException e) {
                    continue;
                }

                Object configValue = configField.get(configuration);
                Object reportedValue = reportedField.get(overview);

                // If values are same do nothing
                if (configValue != reportedValue) {

                    // If change was requested (is pending) update the hw setting, otherwise update database info
                    if (configuration.pending.contains(configFieldName)) {

                        if (this.hardwareInterface != null) {
                            Class type = reportedField.getType();

                            CompletionStage<Message> response;

                            if (type.equals(Boolean.class)) {
                                response = this.hardwareInterface.setParameter(configFieldName, (Boolean) configField.get(configuration));
                            } else if (type.equals(String.class)) {
                                response = this.hardwareInterface.setParameter(configFieldName, (String) configField.get(configuration));
                            } else if (type.equals(Integer.class)) {
                                response = this.hardwareInterface.setParameter(configFieldName, (Integer) configField.get(configuration));
                            } else {
                                logger.warn("configure - unknown parameter: {}", configFieldName);
                                continue;
                            }

                            response.whenComplete((message, exception) -> {
                                if (exception != null) {
                                    logger.internalServerError(exception);
                                }
                            });

                            changeSettings = true;
                            changeConfig = true;
                            configuration.pending.remove(configFieldName);
                        }
                    } else {
                        configField.set(configuration, reportedValue);
                        this.checkSpecial(configFieldName, reportedValue);
                        changeConfig = true;
                    }
                } else if (configuration.pending.contains(configFieldName)) {
                    configuration.pending.remove(configFieldName);
                    changeConfig = true;
                }
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }

        if (changeConfig) {
            this.hardware.update_bootloader_configuration(configuration);
        }

        return changeSettings;
    }

    public void configure(String parameter, Object value) {
        try {
            Field field = configuration.getClass().getField(parameter);
            field.set(configuration, value);

            if (!configuration.pending.contains(parameter)) {
                configuration.pending.add(parameter);
            }

            this.hardware.update_bootloader_configuration(configuration);

            this.checkSpecial(parameter, value);

            if (this.hardwareInterface != null) {

                CompletionStage<Message> response;

                if (value instanceof String) {
                    response = this.hardwareInterface.setParameter(parameter, (String) value);
                } else if (value instanceof Boolean) {
                    response = this.hardwareInterface.setParameter(parameter, (Boolean) value);
                } else if (value instanceof Integer) {
                    response = this.hardwareInterface.setParameter(parameter, (Integer) value);
                } else {
                    throw new NotSupportedException("unknown parameter: " + parameter);
                }

                response.whenComplete((message, exception) -> {
                    if (exception != null) {
                        logger.internalServerError(exception);
                    } else {
                        logger.debug("configure - successfully configured {} = {}", parameter, value);
                    }
                });
            }
        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    public void configure(Swagger_Board_Developer_parameters parameter) {
        try {

            String name = parameter.parameter_type.toLowerCase();

            Field field = configuration.getClass().getField(name);
            Class<?> type = field.getType();

            if (type.equals(Boolean.class)) {
                this.configure(name, parameter.boolean_value);
            } else if (type.equals(String.class)) {
                this.configure(name, parameter.string_value);
            } else if (type.equals(Integer.class)) {
                this.configure(name, parameter.integer_value);
            } else {
                throw new NotSupportedException("unknown parameter: " + name);
            }

        } catch (Exception e) {
            logger.internalServerError(e);
        }
    }

    // TODO this is just temporary, rework
    private void checkSpecial(String parameter, Object value) {
        if (parameter.equals("alias")) {
            if (value instanceof String && !this.hardware.name.equals(value)) {
                this.hardware.name = (String) value;
                this.hardware.update();
            }
        } else if (parameter.equals("autobackup")) {
            if (value instanceof Boolean && this.hardware.backup_mode != (Boolean) value) {
                this.hardware.backup_mode = (Boolean) value;
                this.hardware.update();
            }
        }
    }
}
