package com.redhat.springconsultvaultdemo.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("endpoint")
public class FrontendProperties {
    
    private String japaneseServiceName = null;

    private String frenchServiceName = null;

    private String italianServiceName = null;
    
    private String englishServiceName = null;

    public String getJapaneseServiceName() {
        return japaneseServiceName;
    }

    public void setJapaneseServiceName(String japaneseServiceName) {
        this.japaneseServiceName = japaneseServiceName;
    }

    public String getFrenchServiceName() {
        return frenchServiceName;
    }

    public void setFrenchServiceName(String frenchServiceName) {
        this.frenchServiceName = frenchServiceName;
    }

    public String getItalianServiceName() {
        return italianServiceName;
    }

    public void setItalianServiceName(String italianServiceName) {
        this.italianServiceName = italianServiceName;
    }

    public String getEnglishServiceName() {
        return englishServiceName;
    }

    public void setEnglishServiceName(String englishServiceName) {
        this.englishServiceName = englishServiceName;
    }
}