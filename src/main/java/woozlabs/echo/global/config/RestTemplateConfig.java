package woozlabs.echo.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.apache.hc.client5.http.classic.HttpClient;

import java.sql.Connection;

@Configuration
public class RestTemplateConfig {
    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate buildRestTemplate(RestTemplateBuilder restTemplateBuilder){
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(createHttpClient()));
        return restTemplate;
    }

    private HttpClient createHttpClient(){
        // Connection request timeout
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(5000))
                .build();
        return HttpClientBuilder.create()
                .setConnectionManager(createHttpClientConnectionManager())
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    private HttpClientConnectionManager createHttpClientConnectionManager(){
        // Connect timeout
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(5000))
                .build();

        // Socket timeout
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(5000))
                .build();
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultConnectionConfig(connectionConfig)
                .build();
    }
}
