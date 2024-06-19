package Reusable;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Reusable
{
    public String CreatePostJson(String fname,String lname,String aadharNo,String address,String phone)
    {

        String body = "{\n" +
                "\"Fname\": \""+fname+"\",\n" +
                "\"Lname\": \""+lname+"\",\n" +
                "\"Aadhar_No\": \""+aadharNo+"\",\n" +
                "\"Address\": \""+address+"\",\n" +
                "\"Phone\": \""+phone+"\"\n" +
                "}";
        return body;
    }

    public String readPropertiesfile(String key)
    {
        Properties prop = new Properties();
        String value = null;
        try {
            prop.load(new FileInputStream(System.getProperty("user.dir") + "/aadhartestdata.properties"));
            value = prop.getProperty(key);

        }catch (IOException e)
        {
            e.printStackTrace();
        }
        return value;
    }

    public static String creditCardDetails(String name,int year,long credit_card_no,String limit,String expiry_date,String card_type)
    {
        String body = "{\n" +
                "\"name\": \""+name+"\",\n" +
                "\"data\": {\n" +
                "\"year\": "+year+",\n" +
                "\"Credit Card Number\": "+credit_card_no+",\n" +
                "\"Limit\": \""+limit+"\",\n" +
                "\"EXP Date\": \""+expiry_date+"\",\n" +
                "\"Card Type\": \""+card_type+"\"\n" +
                "}\n" +
                "}";
        return body;
    }

    public String date_extractor(String created_at)
    {
        String[] extracted_date = created_at.split("T");
        return extracted_date[0];
    }

}