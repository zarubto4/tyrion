package utilities.financial.fakturoid.helps_objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import models.Model_InvoiceItem;
import play.data.validation.Constraints;
import utilities.enums.Currency;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Fakturoid_Invoice {
    // needs to be present when we are converting object received from Fakturoid
    @Constraints.Required
    public Long id;

    public String custom_id; // identifikátor faktury ve vaší aplikaci
    public boolean proforma; // příznak proformy
    public boolean  partial_proforma; // přiznak zda je proforma na plnou částku
    public String number; // číslo faktury (Př: 2011-0001, musí odpovídat formátu čísla v nastavení účtu)
    public Long subject_id; // ID kontaktu
    public String currency; // kód měny (nepovinné - doplní se z účtu, 3 znaky)
    public String pdf_url; // API adresa pro stažení faktury v PDF
    public String status; // stav faktury - open/sent/overdue/paid/cancelled
    public Long related_id; // ID proformy/faktury (u faktur vystavených ze zálohových faktur)
    public Date paid_at; // datum a čas zaplacení faktury
    public BigDecimal subtotal;	// součet (bez DPH)	decimal, readonly
    public BigDecimal native_subtotal;	// součet v měně účtu	decimal, readonly
    public BigDecimal total;	// celkový součet (včetně DPH)	decimal, readonly
    public BigDecimal native_total;	// součet v měně účtu (včetně DPH)	decimal, readonly
    public String public_html_url; // veřejná HTML adresa faktury

    public List<Fakturoid_InvoiceItem> lines;
}
