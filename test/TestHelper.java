import junit.framework.TestCase;
import models.person.FloatingPersonToken;
import models.person.Person;
import models.person.ValidationToken;
import models.project.global.financial.GeneralTariff;
import models.project.global.financial.GeneralTariffLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestCase.class);

    public static Person person_create(){

        try{

            Person person = new Person();

            person.nick_name = UUID.randomUUID().toString();
            person.mail = UUID.randomUUID().toString() + "@mail.com";
            person.mailValidated = false;
            person.full_name = UUID.randomUUID().toString();

            person.setSha("password123");
            person.save();
            person.refresh();

            new ValidationToken().setValidation(person.mail);

            return person;
        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }


    public static void person_authenticate(Person person) {

        try {

            person.mailValidated = true;
            person.update();

        }catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
        }
    }

    public static String person_login(Person person) {

        try {

            FloatingPersonToken floatingPersonToken = new FloatingPersonToken();
            floatingPersonToken.set_basic_values();
            floatingPersonToken.person = person;
            floatingPersonToken.user_agent = "Unknown browser";
            floatingPersonToken.save();

            return floatingPersonToken.authToken;
        }catch (Exception e) {
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName(), e.getMessage());
            return null;
        }
    }

    public static void person_delete(Person person){
        try {

            person.delete();

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static GeneralTariff tariff_personal_create(){
        try {

            GeneralTariff general_tariff = new GeneralTariff();

            general_tariff.tariff_name      = UUID.randomUUID().toString();
            general_tariff.identificator    = UUID.randomUUID().toString();
            general_tariff.tariff_description = UUID.randomUUID().toString();

            general_tariff.color            = UUID.randomUUID().toString();

            general_tariff.required_paid_that = true;
            general_tariff.number_of_free_months    = 3;

            general_tariff.company_details_required  = false;
            general_tariff.required_payment_mode     = false;
            general_tariff.required_payment_method   = false;

            general_tariff.credit_card_support      = true;
            general_tariff.bank_transfer_support    = true;

            general_tariff.mode_annually    = true;
            general_tariff.mode_credit      = true;
            general_tariff.free             = true;

            general_tariff.usd = 3.8;
            general_tariff.eur = 3.4;
            general_tariff.czk = 3.0;

            general_tariff.save();
            general_tariff.refresh();

            return general_tariff;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static GeneralTariff tariff_company_create(){
        try {

            GeneralTariff general_tariff = new GeneralTariff();

            general_tariff.tariff_name      = UUID.randomUUID().toString();
            general_tariff.identificator    = UUID.randomUUID().toString();
            general_tariff.tariff_description = UUID.randomUUID().toString();

            general_tariff.color            = UUID.randomUUID().toString();

            general_tariff.required_paid_that = false;
            general_tariff.number_of_free_months    = 3;

            general_tariff.company_details_required  = true;
            general_tariff.required_payment_mode     = true;
            general_tariff.required_payment_method   = true;

            general_tariff.credit_card_support      = true;
            general_tariff.bank_transfer_support    = true;

            general_tariff.mode_annually    = true;
            general_tariff.mode_credit      = true;
            general_tariff.free             = true;

            general_tariff.usd = 3.8;
            general_tariff.eur = 3.4;
            general_tariff.czk = 3.0;

            general_tariff.save();
            general_tariff.refresh();

            return general_tariff;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void tariff_delete(GeneralTariff tariff){
        try {

            tariff.delete();

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }

    public static GeneralTariffLabel tariff_add_label(GeneralTariff tariff){
        try {

            GeneralTariffLabel label = new GeneralTariffLabel();
            label.general_tariff = tariff;
            label.description = UUID.randomUUID().toString();
            label.label = UUID.randomUUID().toString();;
            label.icon = UUID.randomUUID().toString();;

            label.save();
            label.refresh();

            return label;

        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
            return null;
        }
    }

    public static void method(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
    public static void method1(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
    public static void method2(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
    public static void method3(){
        try {



        }catch (Exception e){
            logger.error("!!!! Error while setting up test values. Method {} failed! Reason: {}. This is probably the cause, why following tests failed. !!!!", Thread.currentThread().getStackTrace()[1].getMethodName() , e.getMessage());
        }
    }
}
