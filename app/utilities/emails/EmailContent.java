package utilities.emails;

public class EmailContent{

    StringBuilder emailContent = new StringBuilder();

    public EmailContent startParagraph(String textSize){
        emailContent.append("<p style='font-size:" + textSize + "pt; color: #969696; padding: 10px;'>");
        return this;
    }

    public EmailContent endParagraph(){
        emailContent.append("</p>");
        return this;
    }

    public EmailContent addText(String text){
        emailContent.append(text);
        return this;
    }

    public EmailContent addBoldText(String text){
        emailContent.append("<strong>" + text + "</strong>");
        return this;
    }

    public EmailContent addEmptyLineSpace(){
        emailContent.append("<div style='height: 10px; width: 100%;'></div>");
        return this;
    }

    public EmailContent addLine(){
        emailContent.append("<div style='height: 0px; width: 100%; border-top: 1px solid #eee'></div>");
        return this;
    }

    public EmailContent addLink(String link, String linkName, String textSize){
        emailContent.append("<a href='" + link + "' style='width: 100%; text-decoration: none; text-align: center; font-size:" + textSize + "pt;'>" + linkName + "</a>");
        return this;
    }

    public StringBuilder getEmailContent(){
        return emailContent;
    }
}
