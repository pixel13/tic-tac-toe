package com.github.pixel13.tictactoe.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import lombok.Data;

@Data
public class Cell {

  private int row;
  private int column;
  private String value;

  public static Cell from(Object object) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new Jdk8Module());
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(mapper.writeValueAsString(object), Cell.class);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("Cannot convert given object to Cell");
    }
  }

}
