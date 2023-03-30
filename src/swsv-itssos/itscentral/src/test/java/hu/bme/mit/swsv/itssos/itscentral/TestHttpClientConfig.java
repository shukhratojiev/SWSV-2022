package hu.bme.mit.swsv.itssos.itscentral;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class TestHttpClientConfig {
    // reusing connections can cause NoHttpResponseException as the stubs are reinitialized between requests
    // to avoid this disable connection reuse (keep-alive) in HTTP clients
    // https://www.baeldung.com/httpclient-connection-management
    // https://stackoverflow.com/q/55624675
    // to change retry behavior: http://www.javabyexamples.com/retrying-requests-using-apache-httpclient-4
    @Bean
    public HttpClient testHttpClient() {
        return HttpClientBuilder.create()
                .setConnectionReuseStrategy((r, c) -> false)
                .build();
    }

    // https://springframework.guru/using-resttemplate-with-apaches-httpclient/
    // https://github.com/spring-framework-guru/sfg-blog-posts/blob/90e58ca1e539b76d56a5ee98a4812f59b9169cc5/resttemplate/src/main/java/guru/springframework/resttemplate/config/RestTemplateConfig.java
    // no need to autowire: https://stackoverflow.com/a/57722829
    @Bean
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }
}
