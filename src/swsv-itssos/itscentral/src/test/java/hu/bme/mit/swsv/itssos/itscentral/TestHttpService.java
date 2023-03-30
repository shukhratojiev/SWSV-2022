package hu.bme.mit.swsv.itssos.itscentral;

import eu.arrowhead.common.http.HttpService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;

// force AHT to use this class for outgoing REST requests in tests
@Primary
@Component
public class TestHttpService extends HttpService {
    private static final Logger logger = LogManager.getLogger(TestHttpService.class);

    @Autowired
    private ClientHttpRequestFactory httpRequestFactory;

    // replace HttpClient/HttpRequestFactory in base class to make it configurable
    // https://github.com/arrowhead-f/core-java-spring/blob/f8498dc2d34cfb6cc70190539059d7264b543b69/core-common/src/main/java/eu/arrowhead/common/http/HttpService.java#L191-L204
    @PostConstruct
    @Override
    public void init() throws Exception {
        // super class initializer must be invoked manually
        // https://stackoverflow.com/a/52128565
        super.init();
        logger.info("TestHttpService.init");

        Class<? extends HttpService> cls = HttpService.class;
        Field templateField = cls.getDeclaredField("template");
        templateField.setAccessible(true);
        RestTemplate restTemplate = (RestTemplate) templateField.get(this);
        restTemplate.setRequestFactory(httpRequestFactory);
    }
}
