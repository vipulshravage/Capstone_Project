package StepDef;

import Reusable.Reusable;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.Assert;
import java.io.FileInputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import static io.restassured.RestAssured.given;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CreateNewAccount
{
    public Connection connection;
    private ExtentTest logger;
    private ExtentSparkReporter spark;
    private ExtentReports extent;
    Reusable RA;
    @BeforeClass
    public void report_and_db_setup() throws SQLException
    {
        RA = new Reusable();
        extent = new ExtentReports();
        spark = new ExtentSparkReporter(System.getProperty("user.dir") + "/Report/Aadhar Validation.html");
        spark.config().setDocumentTitle("Aadhar Validation");
        spark.config().setReportName("Aadhar_Details_Validation_Report");
        spark.config().setTheme(Theme.DARK);
        logger = extent.createTest("Validate Aadhar Details");
        extent.attachReporter(spark);
        extent.setSystemInfo("Build_Name", "Capstone Project - Create New Account for Aadhar");
        extent.setSystemInfo("Environment_Name", "QA");
        extent.setSystemInfo("Name", "Vipul Shravage");

        connection =DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "Vipul009"); //driverURL,username,password
        if (connection != null)
        {
            System.out.println("Aadhar Database connection is connected!");
        }
        Statement smt=connection.createStatement();
    }

    @Test(priority = 1)
    public boolean testAadharRecordMatching()
    {
        boolean flag=false;


        //Read properties file
        try {
            Properties properties = new Properties();
        FileInputStream input = new FileInputStream("aadhartestdata.properties");
        properties.load(input);
        input.close();

        //Connect to database
            connection =DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "Vipul009"); //driverURL,username,password
            if (connection != null)
            {
                System.out.println("Aadhar Database connection is connected!");
            }
            Statement smt=connection.createStatement();
            smt.execute("use aadhar_db");


        //Retrieve data from database
        String query = "SELECT * FROM Aadhar WHERE Aadhar_No = '" + properties.getProperty("Aadhar_No") + "'";
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);


        //Compare data
        if (resultSet.next())
        {
            String aadharNo = resultSet.getString("Aadhar_No");
            // Assert that data matches with properties file

            Assert.assertEquals(aadharNo, properties.getProperty("Aadhar_No"));
            System.out.println("Record found in database for Aadhar Number: " +aadharNo);
            flag=true;
        }
        else
        {
            Assert.fail("Record not found in database for Aadhar Number: " + properties.getProperty("Aadhar_No"));
            flag=false;
        }

        // Close database connections
        resultSet.close();
        statement.close();
        connection.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        return flag;
    }

    @Parameters({"posturl"})
    @Test(priority = 2)
    public void postCall(String url) {
        boolean a = testAadharRecordMatching();

        if (a == true) {
            try {
                SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-dd");

                connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306", "root", "Vipul009"); //driverURL,username,password
                if (connection != null) {
                    System.out.println("Connected!");
                }
                Statement smt = connection.createStatement();
                smt.execute("use aadhar_db");

                //Retrieve data from database
                String query = "SELECT * FROM Aadhar WHERE Aadhar_No = '" + RA.readPropertiesfile("Aadhar_No") + "'";


                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    // Retrieve data from the result set
                    String fname = resultSet.getString("Fname");
                    String lname = resultSet.getString("Lname");
                    String aadharNo = resultSet.getString("Aadhar_No");
                    String address = resultSet.getString("Address");
                    String phone = resultSet.getString("Phone");

                    Map<String, String> DBMap = new HashMap<>();
                    // Put the variables into the map
                    DBMap.put("Fname", fname);
                    DBMap.put("Lname", lname);
                    DBMap.put("Aadhar_No", aadharNo);
                    DBMap.put("Address", address);
                    DBMap.put("Phone", phone);

                    Response response = given().header("Content-type", "application/json").
                            body(RA.CreatePostJson(fname, lname, aadharNo, address, phone)).
                            when().
                            post(url);

                    String responseBody = response.getBody().asString();
                    System.out.println("POST Response Body: " + responseBody);

                    //String createdDate=RA.date_extractor(response.getBody().jsonPath().getString("createdAt"));


                    //Read the response and match the response Fname,Lname,Aadhar_No,Address,Phone
                    //data with DB.
                    JsonPath jsonPath = response.jsonPath();
                    Map<String, Object> responseMap = jsonPath.getMap("");
                    String key = null;
                    String value1;
                    Object value2;

                    for (Map.Entry<String, String> entry : DBMap.entrySet()) {
                         key = entry.getKey();
                         value1 = entry.getValue();
                         value2 = responseMap.get(key);
                        try {
                            if (value2 == null) {
                                logger.info("Key '" + key + "' not found in responseMap");
                            } else {
                                Assert.assertEquals(value1, value2, "Values for key '" + key + "'  match");
                                logger.pass("Assertion passed :: " + "Actual ::" + value1 + "Expected:: " + value2);
                            }

                        } catch (AssertionError e) {
                            logger.fail("Assertion failed :: " + "Actual :: " + value1 + "Expected :: " + value2);
                        }

                    }


                    //Validating if Account ID is numeric
                    if (responseMap.containsKey("id"))
                    {
                        Assert.assertTrue(((String) responseMap.get("id")).matches("[0-9]+"), " Account Number is matching ");
                        logger.pass("Value for Id :: "+"is Numeric"+responseMap.get("id"));
                    }

                        //Validating if CreatedAt Field contains current Date
                        Date date = new Date();
                        String current_date = sf.format(date);

                        //Validating createdAt in response and its date should be current date.

                    if(current_date.equals(current_date))
                    {
                        logger.pass("For the field createdAt==> Current-Date: " + current_date + " API Value: " + current_date + " || Value matching.");
                    }
                    else
                    {
                        logger.fail("For the field createdAt==> Current-Date: " + current_date + " API Value: " + current_date + " || Value not matching.");
                    }
                }
            }
            catch (Exception e) {
                System.out.println(e);
            }
        }

        else {
            System.out.println("Aadhar No is not matching !");

        }
    }

    @AfterClass
    public void report_generation()
    {
        System.out.println("All Tests Executed. Extent Report is generated");
        extent.flush();
    }

}