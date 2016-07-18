package utilities.emails;

public class EmailContent{

    String emailContent = "";

    public EmailContent startParagraph(String textSize){
        emailContent += ("<p style='font-size:" + textSize + "pt; color: #969696; padding: 10px;'>");
        return this;
    }

    public EmailContent endParagraph(){
        emailContent += ("</p>");
        return this;
    }

    public EmailContent addText(String text){
        emailContent += (text);
        return this;
    }

    public EmailContent addBoldText(String text){
        emailContent += ("<strong>" + text + "</strong>");
        return this;
    }

    public EmailContent addEmptyLineSpace(){
        emailContent += ("<div style='height: 20px; width: 100%;'></div>");
        return this;
    }

    public EmailContent addLine(){
        emailContent += ("<div style='height: 0px; width: 100%; border-top: 1px solid #eee'></div>");
        return this;
    }

    public EmailContent addLink(String link, String linkName, String textSize){
        emailContent += ("<a href='" + link + "' style='padding: 10px; width: 100% !important; text-decoration: none !important; text-align: center !important; font-size:" + textSize + "pt;'>" + linkName + "</a>");
        return this;
    }

    public String getEmailContent(){
        return emailContent;
    }
}
