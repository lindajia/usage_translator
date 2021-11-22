package main.java;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class UsageTranslator {
  private static final Logger logger = LoggerFactory.getLogger(UsageTranslator.class);
  private static final Map<String, Integer> REDUCTION_RULE = new HashMap<String, Integer>() {{
    put("EA000001GB0O", 1000);
    put("PMQ00005GB0R", 5000);
    put("SSX006NR", 1000);
    put("SPQ00001MB0R", 2000);
  }};
  private static final String SAMPLE_REPORT_FILE = "Sample_Report.csv";
  private static final String TYPE_MAP_FILE = "typemap.json";

  public static void main(String args[]) throws IOException {
    InputParser inputParser = new InputParser(SAMPLE_REPORT_FILE, TYPE_MAP_FILE);
    List<Sample> samples = inputParser.buildSampleList();
    Map<String, String> typeMap = inputParser.buildTypeMap();
    Map<String, Set<String>> domainMap = new HashMap<>();

    String chargeableBatchInsertStmt = buildChargeableBatchInsert(samples, typeMap, REDUCTION_RULE, domainMap);
    String domainsBatchInsertStmt = buildDomainsBatchInsert(domainMap);

    System.out.println(chargeableBatchInsertStmt);
    System.out.println(domainsBatchInsertStmt);

    // Uncomment the following lines to write to files
//    FileWriter chargeableFileWriter = new FileWriter("chargeable.txt");
//    chargeableFileWriter.write(chargeableBatchInsertStmt);
//    chargeableFileWriter.close();
//
//    FileWriter domainsFileWriter = new FileWriter("domains.txt");
//    domainsFileWriter.write(domainsBatchInsertStmt);
//    domainsFileWriter.close();
  }

  /***
   * Build a SQL batch insert statement given a list of samples
   * @param samples A list of samples
   * @param typeMap Map to lookup product by partNumber
   * @param reductionRule Map to lookup rules to map itemCount to usage by product
   * @param domainMap Map for tracking partnerPurchasedPlanID & domains, to be updated as samples are processed
   * @return
   */
  private static String buildChargeableBatchInsert(List<Sample> samples, Map<String, String> typeMap,
                                            Map<String, Integer> reductionRule, Map<String, Set<String>> domainMap) {
    List<String> insertVals = new ArrayList<>();
    // Keeps track of total itemCount per product
    Map<String, Integer> itemCountByProduct = new HashMap<>();
    for (Sample sample : samples) {
      if (sample.getPartNumber().isEmpty() || sample.getPartNumber() == null) {
        logger.error("PartNumber for Chargeable cannot be null, skipping sample {}", sample);
        continue;
      }
      if (sample.getItemCount() < 0) {
        logger.error("Expect item count to be positive, skipping sample {}", sample);
        continue;
      }
      if (InputParser.skipPartnerId(sample.getPartnerId())) {
        logger.debug("Skip based on partnerId for sample {}", sample.getPartnerId());
        continue;
      }
      String product = sample.getProduct(typeMap);
      String partnerPurchasedPlanID = sample.getPartnerPurchasedPlanID();
      int usage = sample.getUsage(reductionRule);
      logItemCountByProduct(itemCountByProduct, product, sample.getItemCount());
      insertVals.add(String.format("(%d, '%s', '%s', '%s', %d)", sample.getPartnerId(), product,
        partnerPurchasedPlanID, sample.getPlan(), usage));
      updateDomainMap(domainMap, partnerPurchasedPlanID, sample.getDomain());
    }
    if (insertVals.size() == 0) {
      return null;
    }
    return StringEscapeUtils.escapeJava(String.format("INSERT INTO chargeable (partnerID, product, " +
        "partnerPurchasedPlanID, plan, usage) values %s;", String.join(",", insertVals)));
  }

  /***
   * Outputs a SQL String to create 1 entry in the domains table for every unique partnerPurchasedPlanID & domain combo
   * @param domainsMap Key: partnerPurchasedPlanID, Value: unique domains associated with the key
   * @return SQL String for updating domains table
   */
  private static String buildDomainsBatchInsert(Map<String, Set<String>> domainsMap) {
    List<String> insertVals = new ArrayList<>();
    // Prepare a value for SQL insertion for every unique partnerPurchasedPlanID and domain combo
    for (Map.Entry<String, Set<String>> entry : domainsMap.entrySet()) {
      for (String domain : entry.getValue()) {
        insertVals.add(String.format("('%s', '%s')", entry.getKey(), domain));
      }
    }
    if (insertVals.size() == 0) {
      return null;
    }
    return StringEscapeUtils.escapeJava(String.format("INSERT INTO domains (partnerPurchasedPlanID, domain) VALUES %s;",
      String.join(",", insertVals)));
  }

  private static void logItemCountByProduct(Map<String, Integer> itemCountByProduct, String product, int itemCount) {
    if (itemCountByProduct.containsKey(product)) {
      itemCount += itemCountByProduct.get(product);
    }
    itemCountByProduct.put(product, itemCount);
    logger.info(String.format("Updated itemCount for product %s, new itemCount = %d", product, itemCount));
  }

  private static void updateDomainMap(Map<String, Set<String>> domainMap, String partnerPurchasedPlanID, String domain) {
    if (!domainMap.containsKey(partnerPurchasedPlanID)) {
      domainMap.put(partnerPurchasedPlanID, new HashSet<String>() {{ add(domain); }});
    } else {
      Set<String> domains = domainMap.get(partnerPurchasedPlanID);
      domains.add(domain);
      domainMap.put(partnerPurchasedPlanID, domains);
    }
  }
}
