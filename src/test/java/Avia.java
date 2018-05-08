import atu.testrecorder.ATUTestRecorder;
import atu.testrecorder.exceptions.ATUTestRecorderException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import java.util.logging.FileHandler;

public class Avia {

    public static WebDriver driver;
    //Определяем первую страницу для входа  на сайт.
    private static String baseurl = "https://avia.tutu.ru";
    private static String from = "Калуга (Россия)";
    private static String to = "Прага (Чехия)";
    WebDriverWait wait = new WebDriverWait(driver, 40);

    @BeforeClass

    public static void setUp() throws Exception {

        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        //Задаем неявную задержку
        driver.manage().timeouts().implicitlyWait(40, TimeUnit.SECONDS);
    }

    @Test public void Head() throws IOException, InterruptedException, ATUTestRecorderException {

        ATUTestRecorder recorder = new ATUTestRecorder("ScriptVideos", false);
        recorder.start();

        FileWriter wri = new FileWriter("NaN.txt", true);

        //Идем на страницу входа
        driver.navigate().to(baseurl);

        //Вводим данные для поиска маршрута. Можно не выбирать из выпадающего списка
        driver.findElement(By.name("city_from")).sendKeys(from);
        driver.findElement(By.name("city_from")).sendKeys(Keys.ENTER);

        //Город прилета нужно обязательно выбирать из выпадающего списка, при вводе строки не переходит к следующему окну
        driver.findElement(By.name("city_to")).sendKeys(to);
        driver.findElement(By.name("city_to")).click();
        driver.findElement(By.cssSelector("span.name_city")).click();

        //Выбираем дату сегодня
        driver.findElement(By.xpath("//*[@id='ui-datepicker-div']/div[2]")).click();

        //Ищем билеты
        driver.findElement(By.cssSelector("div.complex_route > div > button")).click();
        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.cssSelector("li.service_item.m-near_cities"))));

        //???? Иногда страница открывается с уже нажатой этой кнопкой ???????
        //driver.findElement(By.cssSelector("li.service_item.m-near_cities")).click();

        driver.findElement(By.xpath("//*[@id='near_cities']/div[5]/table/tbody/tr[1]/td[1]/div/div[1]/span")).click();

        //Прокручиваем страницу, чтобы была видна на экране последняя строка таблицы
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        js.executeScript("window.scrollTo(0, 200);");

        //Делаем скриншот, чтобы была видна таблица с багами
        File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        File screenShotFile = new File("Screen_near_cities.png");
        org.openqa.selenium.io.FileHandler.copy(scrFile, screenShotFile);


        WebElement nearCities = driver.findElement(By.cssSelector("#near_cities > div:nth-child(5)"));
        List<WebElement> prices = nearCities.findElements(By.cssSelector("div.j-price.j-direct_price"));
        List<WebElement> pricesAll = nearCities.findElements(By.cssSelector("div.j-price.j-all_price"));
        List<WebElement> rows = nearCities.findElements(By.cssSelector("tr.j-row.group_row"));

        for (int i = 0; i < rows.size(); i++) {
            String row = rows.get(i).getAttribute("outerText");
            String price1 = prices.get(i).getAttribute("outerText");
            String priceAll1 = pricesAll.get(i).getAttribute("outerText");
            wri.write(row + "\n");
            wri.flush();
            System.out.println(row);

            //Проверяем, что есть рейсы для данного маршрута
            if (price1.equals("нет рейсов") && priceAll1.equals("нет рейсов")) {
                System.out.println("нет рейсов");
            } else {

                //С помощью регулярных выражений проверяем, что в поле с ценами содержатся цифры
                String pattern = "([0-9])";
                Pattern p = Pattern.compile(pattern);
                String price2 = prices.get(i).getAttribute("outerText");
                String priceAll2 = pricesAll.get(i).getAttribute("outerText");

                System.out.println(price2);
                Matcher mPrice = p.matcher(price2);
                Matcher mPriceAll = p.matcher(priceAll1);
                StringBuilder sbPrice = new StringBuilder();
                StringBuilder sbPriceAll = new StringBuilder();

                while (mPrice.find()) {
                    String nomber;
                    nomber = price2.substring(mPrice.start(), mPrice.end());
                    sbPrice.append(nomber);
                }

                while (mPriceAll.find()){
                    String nomberAll;
                    nomberAll = priceAll2.substring(mPriceAll.start(), mPriceAll.end());
                    sbPriceAll.append(nomberAll);
                }
                System.out.println("___" + sbPrice + "______" + sbPriceAll);
                if (sbPrice.length() == 0 | sbPriceAll.length() == 0) {
                    System.out.println("Price Error");
                }
                System.out.println();
            }
        }

        //Останавливаем видеозапись
        recorder.stop();
    }

    @After public void stop(){
        driver.quit();
        driver = null;
    }


}
