package utilities.financial.fakturoid.helps_objects;

import play.data.validation.Constraints;

public class Fakturoid_Subject {
    // needs to be present when we are converting object received from Fakturoid
    @Constraints.Required
    public Long id;

    public String custom_id; //identifikátor kontaktu ve vaší aplikaci
    public String type = "customer"; //typ kontaktu - customer/supplier/both
    public String name;
    public String street;
    public String street2;
    public String city; //
    public String zip; // poštovní směrovací číslo
    public String country; // země (ISO Kód)
    public String registration_no; // 	IČ podnikatele
    public String vat_no; // DIČ (plátci DPH, IČ DPH na Slovensku, je mezinárodní a začíná kódem země)
    public String local_vat_no; // SK DIČ (pouze pro Slovensko, nezačíná kódem země)
    public String bank_account; //
    public String iban; //
    public String variable_symbol; //
    public boolean enabled_reminders = false; // příznak zda zasílat připomínky
    public String full_name; // jméno kontaktní osoby
    public String email; // email pro posílání faktur kontaktu
    public String email_copy; // email pro posílání kopie faktur kontaktu
    public String phone;
    public String web;

}