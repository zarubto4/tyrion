package utilities.goPay.helps_objects;


import utilities.enums.Recurrence_cycle;

public class Recurrence {
    public Recurrence_cycle recurrence_cycle;
   // public Integer recurrence_period = 1; Určovalo kolikreát za periodu, ale jelikož máme pouze ON_DEMAND tak není třeba zasílat
    public String recurrence_date_to =  "2030-10-10"; // DLouhá jistota - prostě konstanta
    //public String recurrence_state = "REQUESTED"; // Doplňujese samo
}

