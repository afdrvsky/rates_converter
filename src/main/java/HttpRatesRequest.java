import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HttpRatesRequest {

    private static final String CBR_URL = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=";

    private static final ArrayList<Rates> rates = new ArrayList<>();

    private String getActualDate() {
        Date actualDateTime = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        return dateFormat.format(actualDateTime);
    }

    private HttpResponse<String> httpReq(String url) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public String getTodayRates() throws IOException, InterruptedException {
        HttpResponse<String> todayRates = httpReq(CBR_URL + getActualDate());
        return todayRates.body();
    }

    public void parseXmlRates() throws ParserConfigurationException, IOException, InterruptedException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(getTodayRates())));
        NodeList numCodeElements = document.getDocumentElement().getElementsByTagName("NumCode");
        NodeList charCodeElements = document.getDocumentElement().getElementsByTagName("CharCode");
        NodeList nominalElements = document.getDocumentElement().getElementsByTagName("Nominal");
        NodeList nameElements = document.getDocumentElement().getElementsByTagName("Name");
        NodeList valueElements = document.getDocumentElement().getElementsByTagName("Value");
        for (int i = 0; i < numCodeElements.getLength(); i++) {
            String numCode = numCodeElements.item(i).getFirstChild().getNodeValue();
            String charCode = charCodeElements.item(i).getFirstChild().getNodeValue();
            String nominal = nominalElements.item(i).getFirstChild().getNodeValue();
            String name = nameElements.item(i).getFirstChild().getNodeValue();
            String value = valueElements.item(i).getFirstChild().getNodeValue().replace(",", ".");
            rates.add(new Rates(numCode, charCode, nominal, name, value));
        }

        for (Rates rates : rates) {
            System.out.println(String.format("Код валюты - %s, буквенный код - %s, %s %s = %s Российских рублей",
                    rates.getNumCode(), rates.getCharCode(), rates.getNominal(), rates.getName(), rates.getValue()));
        }
    }

    public void convertToRouble(String value, String charCode) throws Exception {
        Double rate = null;
        for (int i = 0; i < rates.size(); i++) {
            if (rates.get(i).getCharCode().equals(charCode)) {
                rate = Double.parseDouble(rates.get(i).getValue());
                Double nominal = Double.parseDouble(rates.get(i).getNominal());
                Double val = Double.parseDouble(value);
                Double result = val * rate / nominal;
                System.out.println(String.format("За %s %s можно получить %.2f Российских рублей", value, charCode, result));
            }
        }
        if (rate == null) {
            throw new Exception(String.format("Для валюты %s не найден курс валют!", charCode));
        }
    }

    public void convertFromRouble(String value, String charCode) throws Exception {
        Double rate = null;
        for (int i = 0; i < rates.size(); i++) {
            if (rates.get(i).getCharCode().equals(charCode)) {
                rate = Double.parseDouble(rates.get(i).getValue());
                Double val = Double.parseDouble(value);
                Double result = val / rate;
                System.out.println(String.format("За %s Российских рублей можно получить %.2f %s", value, result, charCode));
            }
        }
        if (rate == null) {
            throw new Exception(String.format("Для валюты %s не найден курс валют!", charCode));
        }
    }
}
