package main.java;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
public class Sample {
  @CsvBindByName(column = "PartnerID")
  private int partnerId;
  @CsvBindByName(column = "partnerGuid")
  private String partnerGuid;
  @CsvBindByName(column = "accountid")
  private int accountId;
  @CsvBindByName(column = "accountGuid")
  private String accountGuid;
  @CsvBindByName(column = "username")
  private String userName;
  @CsvBindByName(column = "domains")
  private String domain;
  @CsvBindByName(column = "itemname")
  private String itemname;
  @CsvBindByName(column = "plan")
  private String plan;
  @CsvBindByName(column = "itemType")
  private int itemType;
  @CsvBindByName(column = "PartNumber")
  private String partNumber;
  @CsvBindByName(column = "itemCount")
  private int itemCount;

  public String toString() {
    return String.format("partnerId = %d, partnerGuid = %s, accountid = %d, accountGuid = %s, username = %s," +
        "domains = %s, itemname = %s, plan = %s, itemType = %d, partNumber = %s, itemCount = %d", partnerId,
      partnerGuid, accountId, accountGuid, userName, domain, itemname, plan, itemType, partNumber, itemCount);
  }

  public String getPartnerPurchasedPlanID() {
    // strip non-alphanumeric characters
    return accountGuid.replaceAll("[^A-Za-z0-9]", "");
  }

  public String getProduct(Map<String, String> typeMap) {
    return typeMap.get(partNumber);
  }

  /*** If there exists a reduction rule for the sample's partNumber, usage = sample itemCount / reduction rule amount
   * If a reduction rule for the sample's is not found, usage = itemCount
   */
  public int getUsage(Map<String, Integer> reductionRule) {
    if (reductionRule.get(partNumber) != null) {
      return itemCount / reductionRule.get(partNumber);
    }
    return itemCount;
  }
}
