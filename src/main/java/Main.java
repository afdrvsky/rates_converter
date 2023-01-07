public class Main {

    public static void main(String[] args) throws Exception {
        HttpRatesRequest httpRatesRequest = new HttpRatesRequest();
        httpRatesRequest.parseXmlRates();
        httpRatesRequest.convertToRouble("100", "CHF");
        httpRatesRequest.convertFromRouble("250000", "BYN");
    }
}
