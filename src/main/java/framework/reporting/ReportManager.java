package framework.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.http.Header;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public final class ReportManager {

    private static volatile ExtentReports extent;
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");

    private ReportManager() {}

    public static synchronized ExtentReports init(String reportName, String docTitle) {
        if (extent != null) return extent;

        Path reportPath = Paths.get(System.getProperty("user.dir"), "reports",
                "TestReport-" + LocalDateTime.now().format(STAMP) + ".html");
        reportPath.getParent().toFile().mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath.toString());
        spark.config().setEncoding("utf-8");
        spark.config().setDocumentTitle(docTitle);
        spark.config().setReportName(reportName);
        spark.config().setTheme(Theme.DARK);

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
        extent.setSystemInfo("User", System.getProperty("user.name"));
        extent.setSystemInfo("Profile",
                System.getProperty("profile", System.getProperty("env", "default")));
        return extent;
    }

    public static void flush() {
        if (extent != null) extent.flush();
    }

    public static void setCurrent(ExtentTest test) { CURRENT.set(test); }
    public static void clearCurrent() { CURRENT.remove(); }
    public static ExtentTest current() { return CURRENT.get(); }

    public static ExtentTest createTest(String name, String description) {
        ExtentTest test = init("Test", "Report").createTest(name, description);
        setCurrent(test);
        return test;
    }

    public static void info(String log) {
        if (current() != null)
            current().info(MarkupHelper.createLabel(log, ExtentColor.TEAL));
    }

    public static void pass(String log) {
        if (current() != null)
            current().pass(MarkupHelper.createLabel(log, ExtentColor.GREEN));
    }

    public static void fail(String log) {
        if (current() != null)
            current().fail(MarkupHelper.createLabel(log, ExtentColor.RED));
    }

    public static void json(String json) {
        if (current() != null)
            current().info(MarkupHelper.createCodeBlock(json, CodeLanguage.JSON));
    }

    public static void curl(String curl) {
        if (current() != null && curl != null)
            current().log(Status.INFO, "<pre>" + escape(curl) + "</pre>");
    }

    public static void headers(List<Header> headers) {
        if (current() == null || headers == null || headers.isEmpty()) return;
        String[][] table = headers.stream()
                .map(h -> new String[]{h.getName(), h.getValue()})
                .toArray(String[][]::new);
        current().info(MarkupHelper.createTable(table));
    }

    private static String escape(String s) {
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }
}
