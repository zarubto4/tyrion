package utilities.goPay.helps_objects;


import models.project.global.Product;

public class Recurrence {
    public Product.Recurrence_cycle recurrence_cycle;
   // public Integer recurrence_period = 1; Určovalo kolikreát za periodu, ale jelikož máme pouze ON_DEMAND tak není třeba zasílat
    public String recurrence_date_to =  "2030-10-10"; // DLouhá jistota - prostě konstanta
    //public String recurrence_state = "REQUESTED"; // Doplňujese samo
}

