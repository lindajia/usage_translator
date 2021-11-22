package main.java;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class InputParser {
  private final String sampleReportFile;
  private String typeMapFile;

  public InputParser(String sampleReportFile, String typeMapFile) {
    this.sampleReportFile = sampleReportFile;
    this.typeMapFile = typeMapFile;
  }

  public List<Sample> buildSampleList() {
    List<Sample> samples = new CsvToBeanBuilder(new InputStreamReader(
      getClass().getClassLoader().getResourceAsStream(sampleReportFile)))
      .withType(Sample.class).build().parse();
    return samples;
  }

  public Map<String, String> buildTypeMap() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    final Map<String, String> typeMap = mapper.readValue(
      getClass().getClassLoader().getResourceAsStream(typeMapFile), Map.class);
    return typeMap;
  }

  public static Boolean skipPartnerId(int partnerId) {
    return partnerId == 26392;
  }
}
