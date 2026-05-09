package org.instalk.cloud.instalkaiservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai")
public class AiProviderProperties {

    private String key;
    private String url;
    private String embeddingUrl;
    private String embeddingModel;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmbeddingUrl() {
        return embeddingUrl;
    }

    public void setEmbeddingUrl(String embeddingUrl) {
        this.embeddingUrl = embeddingUrl;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

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
