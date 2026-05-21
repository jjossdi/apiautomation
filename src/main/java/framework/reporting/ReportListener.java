package framework.reporting;

import com.aventstack.extentreports.ExtentTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

public class ReportListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ReportManager.init("API Automation Report", "Test Execution Report");
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        Object[] params = result.getParameters();
        if (params != null && params.length > 0) {
            name = name + " [" + safeParamLabel(params) + "]";
        }
        ExtentTest test = ReportManager.createTest(name, desc);
        test.assignCategory(result.getTestClass().getRealClass().getSimpleName());
        String[] groups = result.getMethod().getGroups();
        if (groups != null) {
            for (String g : groups) test.assignCategory(g);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ReportManager.pass("Test passed in " + (result.getEndMillis() - result.getStartMillis()) + "ms");
        ReportManager.clearCurrent();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Throwable t = result.getThrowable();
        if (t != null) {
            ReportManager.fail(t.getMessage() == null ? t.toString() : t.getMessage());
            String trace = Arrays.toString(t.getStackTrace()).replace(",", "<br>");
            if (ReportManager.current() != null) {
                ReportManager.current().fail("<details><summary>Stack trace</summary>" + trace + "</details>");
            }
        }
        ReportManager.clearCurrent();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (ReportManager.current() != null) {
            ReportManager.current().skip("Skipped: " +
                    (result.getThrowable() != null ? result.getThrowable().getMessage() : "no reason"));
        }
        ReportManager.clearCurrent();
    }

    @Override
    public void onFinish(ITestContext context) {
        ReportManager.flush();
    }

    private static String safeParamLabel(Object[] params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) sb.append(", ");
            String p = String.valueOf(params[i]);
            if (p.length() > 40) p = p.substring(0, 40) + "...";
            sb.append(p);
        }
        return sb.toString();
    }
}
