package com.zencode.book.email;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class EmailDto {
    private String subject;
    private String to;
    private EmailTemplateName emailTemplateName;
    private Map<String, Object> properties;
}
