package uk.ac.cam.gp.charlie;

import java.util.ArrayList;
import java.util.List;

public class TestResults {

  public String testName = "Unnamed Test";
  public List<String> summaryLogs = new ArrayList<>();
  public List<String> failedTests = new ArrayList<>();
  private int failed;
  private int total;
  private int id = 1;

  public TestResults(String name, int testcount) {
    this.testName = name;
    this.total = testcount;
    summaryLogs.add("\u001b[36m" + name + "\u001b[0m Summary:");
  }

  public void addFailedTest(String query) {
    String result ="\u001b[36m" + testName + "\u001b[0m(" + (id) + "/"
        + total + ") - \u001b[35m" + query + "\u001b[0m";
    failedTests.add(result);
    result = "Test \u001b[31mFailed\u001b[0m "+result;
    System.out.println(result);
    summaryLogs.add("  "+result);
    failed++;
    id++;
  }
  public void addPassedTest(String query) {
    String result = "Test \u001b[32mPassed\u001b[0m \u001b[36m" + testName + "\u001b[0m(" + (id++) + "/"
        + total + ") - \u001b[35m" + query + "\u001b[0m";
    System.out.println(result);
    summaryLogs.add("  "+result);
  }

  public String getConclusion() {
    if (getDidAllPass()) {
      return "\u001b[32mAll Tests Passed\u001b[0m";
    } else {
      return "\u001b[31m"+getFailed()+"/"+total+"  Tests Failed\u001b[0m";
    }
  }

  public void soutAll() {
    for (String log:summaryLogs) {
      System.out.println(log);
    }
    System.out.println(getConclusion());
  }

  public int getFailed() {
    return failed;
  }
  public int getTotal() {
    return total;
  }
  public int getPassed() {
    return total-failed;
  }
  public boolean getDidAllPass() {
    return failed==0;
  }


}
