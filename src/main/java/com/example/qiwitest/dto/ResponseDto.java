package com.example.qiwitest.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "response")
public class ResponseDto {

    @JacksonXmlProperty(localName = "result-code")
    private Integer resultCode;

    @JacksonXmlProperty(localName = "extra")
    private List<ExtraDto> extras;

    public ResponseDto() {
        this.extras = new ArrayList<>();
    }

    public ResponseDto(Integer resultCode) {
        this();
        this.resultCode = resultCode;
    }

    public Integer getResultCode() {
        return resultCode;
    }

    public void setResultCode(Integer resultCode) {
        this.resultCode = resultCode;
    }

    public List<ExtraDto> getExtras() {
        return extras;
    }

    public void setExtras(List<ExtraDto> extras) {
        this.extras = extras;
    }

    public void addExtra(String name, String value) {
        ExtraDto extra = new ExtraDto();
        extra.setName(name);
        extra.setValue(value);
        this.extras.add(extra);
    }
}