package com.ka1zerov.nbd.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PubSubMessage<T> {
    private String type;
    private T data;
}
