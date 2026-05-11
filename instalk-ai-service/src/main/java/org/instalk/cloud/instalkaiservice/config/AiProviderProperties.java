package org.instalk.cloud.instalkaiservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {

    private String key;
    private String url;
    private String embeddingUrl;
    private String embeddingModel;
    public String chatBaseUrl() {
        return stripLastPathSegment(url);
    }

    public String embeddingBaseUrl() {
        return stripLastPathSegment(embeddingUrl);
    }

    private static String stripLastPathSegment(String endpointUrl) {
        if (endpointUrl == null || endpointUrl.isBlank()) {
            return endpointUrl;
        }
        String u = endpointUrl.stripTrailing();
        int last = u.lastIndexOf('/');
        return last > 0 ? u.substring(0, last) : u;
    }
}
