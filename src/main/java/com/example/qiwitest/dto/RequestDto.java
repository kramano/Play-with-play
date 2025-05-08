package com.example.qiwitest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "request")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestDto {

    @JacksonXmlProperty(localName = "request-type")
    private String requestType;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "extra")
    private List<ExtraDto> extras;

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<ExtraDto> getExtras() {
        return extras;
    }

    public void setExtras(List<ExtraDto> extras) {
        this.extras = extras;
    }

    public String getExtraValue(String name) {
        if (extras == null) {
            return null;
        }
        return extras.stream()
                .filter(extra -> name.equals(extra.getName()))
                .map(ExtraDto::getValue)
                .findFirst()
                .orElse(null);
    }
}