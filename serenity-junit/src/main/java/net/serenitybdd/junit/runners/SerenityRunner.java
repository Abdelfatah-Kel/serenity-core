package net.serenitybdd.junit.runners;

import com.google.common.base.Optional;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.core.injectors.EnvironmentDependencyInjector;
import net.thucydides.core.ThucydidesSystemProperty;
import net.thucydides.core.annotations.ManagedWebDriverAnnotatedField;
import net.thucydides.core.annotations.TestCaseAnnotations;
import net.thucydides.core.batches.BatchManager;
import net.thucydides.core.batches.BatchManagerProvider;
import net.thucydides.core.guice.Injectors;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import net.thucydides.core.pages.Pages;
import net.thucydides.core.reports.AcceptanceTestReporter;
import net.thucydides.core.reports.ReportService;
import net.thucydides.core.statistics.TestCount;
import net.thucydides.core.steps.PageObjectDependencyInjector;
import net.thucydides.core.steps.StepAnnotations;
import net.thucydides.core.steps.StepEventBus;
import net.thucydides.core.steps.StepFactory;
import net.thucydides.core.steps.stepdata.StepData;
import net.thucydides.core.tags.TagScanner;
import net.thucydides.core.webdriver.*;
import net.thucydides.junit.listeners.JUnitStepListener;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static net.serenitybdd.core.Serenity.initializeTestSession;

/**
 * A test runner for WebDriver-based web tests. This test runner initializes a
 * WebDriver instance before running the tests in their order of appearance. At
 * the end of the tests, it closes and quits the WebDriver instance.
 * The test runner will by default produce output in XML and HTML. This
 * can extended by subscribing more reporter implementations to the test runner.
 *
 * @author johnsmart
 */
public class SerenityRunner extends BlockJUnit4ClassRunner {

    /**
     * Provides a proxy of the ScenarioSteps object used to invoke the test steps.
     * This proxy notifies the test runner about individual step outcomes.
     */
    private StepFactory stepFactory;
    private Pages pages;
    private final WebdriverManager webdriverManager;
    private String requestedDriver;
    private ReportService reportService;
    private final TestCount testCount;

    private final TestConfiguration theTest;
    private String qualifier;
    /**
     * Special listener that keeps track of test step execution and results.
     */
    private JUnitStepListener stepListener;

    private PageObjectDependencyInjector dependencyInjector;

    /**
     * Retrieve the runner getConfiguration().from an external source.
     */
    private Configuration configuration;

    private TagScanner tagScanner;

    private BatchManager batchManager;

    private final Logger logger = LoggerFactory.getLogger(SerenityRunner.class);

    public Pages getPages() {
        return pages;
    }

    /**
     * Creates a new test runner for WebDriver web tests.
     *
     * @param klass the class under test
     * @throws org.junit.runners.model.InitializationError if some JUnit-related initialization problem occurred
     */
    public SerenityRunner(final Class<?> klass) throws InitializationError {
        this(klass, Injectors.getInjector());
    }

    /**
     * Creates a new test runner for WebDriver web tests.
     *
     * @param klass the class under test
     * @param module used to inject a custom Guice module
     * @throws org.junit.runners.model.InitializationError if some JUnit-related initialization problem occurred
     */
    public SerenityRunner(Class<?> klass, Module module) throws InitializationError {
        this(klass, Injectors.getInjector(module));
    }

    public SerenityRunner(final Class<?> klass,
                          final Injector injector) throws InitializationError {
        this(klass,
                injector.getInstance(WebdriverManager.class),
                injector.getInstance(Configuration.class),
                injector.getInstance(BatchManager.class)
        );
    }

    public SerenityRunner(final Class<?> klass,
                          final WebDriverFactory webDriverFactory) throws InitializationError {
        this(klass, webDriverFactory, Injectors.getInjector().getInstance(Configuration.class));
    }

    public SerenityRunner(final Class<?> klass,
                          final WebDriverFactory webDriverFactory,
                          final Configuration configuration) throws InitializationError {
        this(klass,
                webDriverFactory,
                configuration,
                new BatchManagerProvider(configuration).get()
        );
    }

    public SerenityRunner(final Class<?> klass,
                          final WebDriverFactory webDriverFactory,
                          final Configuration configuration,
                          final BatchManager batchManager) throws InitializationError {
        this(klass,
                new ThucydidesWebdriverManager(webDriverFactory, configuration),
                configuration,
                batchManager
        );
    }

    public SerenityRunner(final Class<?> klass, final BatchManager batchManager) throws InitializationError {
        this(klass,
                Injectors.getInjector().getInstance(WebdriverManager.class),
                Injectors.getInjector().getInstance(Configuration.class),
                batchManager);
    }

    public SerenityRunner(final Class<?> klass,
                          final WebdriverManager webDriverManager,
                          final Configuration configuration,
                          final BatchManager batchManager) throws InitializationError {
        super(klass);

        this.theTest = TestConfiguration.forClass(klass).withSystemConfiguration(configuration);
        this.webdriverManager = webDriverManager;
        this.configuration = configuration;
        this.requestedDriver = getSpecifiedDriver(klass);
        this.tagScanner = new TagScanner(configuration.getEnvironmentVariables());

        this.testCount = Injectors.getInjector().getInstance(TestCount.class);

        if (TestCaseAnnotations.supportsWebTests(klass)) {
            checkRequestedDriverType();
        }

        this.batchManager = batchManager;

        batchManager.registerTestCase(klass);

    }


    private String getSpecifiedDriver(Class<?> klass) {
        if (ManagedWebDriverAnnotatedField.hasManagedWebdriverField(klass)) {
            return ManagedWebDriverAnnotatedField.findFirstAnnotatedField(klass).getDriver();
        } else {
            return null;
        }
    }

    /**
     * The Configuration class manages output directories and driver types.
     * They can be defined as system values, or have sensible defaults.
     * @return the current configuration
     */
    protected Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Batch Manager used for running tests in parallel batches
     * @return the current batch manager object
     */
    protected BatchManager getBatchManager() {
        return batchManager;
    }

    /**
     * Ensure that the requested driver type is valid before we start the tests.
     * Otherwise, throw an InitializationError.
     */
    private void checkRequestedDriverType() {
        if (requestedDriverSpecified()) {
            SupportedWebDriver.getDriverTypeFor(requestedDriver);
        } else {
            getConfiguration().getDriverType();
        }
    }

    private boolean requestedDriverSpecified() {
        return !StringUtils.isEmpty(this.requestedDriver);
    }

    public File getOutputDirectory() {
        return getConfiguration().getOutputDirectory();
    }

    /**
     * To generate reports, different AcceptanceTestReporter instances need to
     * subscribe to the listener. The listener will tell them when the test is
     * done, and the reporter can decide what to do.
     * @param reporter an implementation of the AcceptanceTestReporter interface.
     */
    public void subscribeReporter(final AcceptanceTestReporter reporter) {
        getReportService().subscribe(reporter);
    }

    public void useQualifier(final String qualifier) {
        this.qualifier = qualifier;
        getReportService().useQualifier(qualifier);
    }

    public String getQualifier(){
        return qualifier;
    }

    /**
     * Runs the tests in the acceptance test case.
     */

    @Override
    public void run(final RunNotifier notifier) {
        if (skipThisTest()) { return; }

        try {
            RunNotifier localNotifier = initializeRunNotifier(notifier);
            super.run(localNotifier);
            fireNotificationsBasedOnTestResultsTo(notifier);
        } catch (Throwable someFailure) {
            someFailure.printStackTrace();
            throw someFailure;
        } finally {
            notifyTestSuiteFinished();
            generateReports();
            dropListeners(notifier);
            closeDrivers();
        }
    }

    private Optional<TestOutcome> latestOutcome() {
        if (StepEventBus.getEventBus().getBaseStepListener().getTestOutcomes().isEmpty()) {
            return Optional.absent();
        }
        return Optional.of(StepEventBus.getEventBus().getBaseStepListener().getTestOutcomes().get(0));
    }

    private void fireNotificationsBasedOnTestResultsTo(RunNotifier notifier) {
        if (!latestOutcome().isPresent()) {
            return;
        }
        if (latestOutcome().get().getResult() == TestResult.IGNORED || latestOutcome().get().getResult() == TestResult.PENDING) {
            notifier.fireTestIgnored(getDescription());
        }
    }

    private void notifyTestSuiteFinished() {
        try {
            StepEventBus.getEventBus().testSuiteFinished();
        } catch (Throwable listenerException) {
            // We report and ignore listener exceptions so as not to mess up the rest of the test mechanics.
            logger.error("Test event bus error: " + listenerException.getMessage(), listenerException);
        }
    }

    private void dropListeners(final RunNotifier notifier) {
        JUnitStepListener listener = getStepListener();
        notifier.removeListener(listener);
        getStepListener().dropListeners();
    }

    protected void generateReports() {
        generateReportsFor(getTestOutcomes());
    }

    private boolean skipThisTest() {
        return testNotInCurrentBatch();
    }

    private boolean testNotInCurrentBatch() {
        return (batchManager != null) && (!batchManager.shouldExecuteThisTest(getDescription().testCount()));
    }

    /**
     * The Step Listener observes and records what happens during the execution of the test.
     * Once the test is over, the Step Listener can provide the acceptance test outcome in the
     * form of an TestOutcome object.
     * @return the current step listener
     */
    protected JUnitStepListener getStepListener() {
        if (stepListener == null) {
            buildAndConfigureListeners();
        }
        return stepListener;
    }

    protected void setStepListener(JUnitStepListener stepListener) {
        this.stepListener = stepListener;
    }

    private void buildAndConfigureListeners() {

        initStepEventBus();
        if (webtestsAreSupported()) {
            initPagesObjectUsing(webdriverManager.getWebdriver(requestedDriver));
            setStepListener(initListenersUsing(getPages()));
            initStepFactoryUsing(getPages());
        } else {
            setStepListener(initListeners());
            initStepFactory();
        }
    }

    private RunNotifier initializeRunNotifier(RunNotifier notifier) {
        if (theTest.shouldRetryTest()) {
            notifier.addListener(getStepListener());
            return notifier;
        } else {
            RunNotifier notifierForSteps = new RunNotifier();
            notifierForSteps.addListener(getStepListener());
            return new RetryFilteringRunNotifier(notifier, notifierForSteps);
        }
    }

    private boolean shouldRetryTest() {
        return (ThucydidesSystemProperty.JUNIT_RETRY_TESTS.booleanFrom(configuration.getEnvironmentVariables()));
    }

    protected void initStepEventBus() {
        StepEventBus.getEventBus().clear();
    }

    private void initPagesObjectUsing(final WebDriver driver) {
        pages = new Pages(driver, getConfiguration());
        dependencyInjector = new PageObjectDependencyInjector(pages);
    }

    protected JUnitStepListener initListenersUsing(final Pages pageFactory) {

        return JUnitStepListener.withOutputDirectory(getConfiguration().getOutputDirectory())
                .and().withPageFactory(pageFactory)
                .and().withTestClass(getTestClass().getJavaClass())
                .and().build();
    }

    protected JUnitStepListener initListeners() {
        return JUnitStepListener.withOutputDirectory(getConfiguration().getOutputDirectory())
                .and().withTestClass(getTestClass().getJavaClass())
                .and().build();
    }

    private boolean webtestsAreSupported() {
        return TestCaseAnnotations.supportsWebTests(this.getTestClass().getJavaClass());
    }

    private void initStepFactoryUsing(final Pages pagesObject) {
        stepFactory = new StepFactory(pagesObject);
    }

    private void initStepFactory() {
        stepFactory = new StepFactory();
    }

    private void closeDrivers() {
        getWebdriverManager().closeAllCurrentDrivers();
    }

    protected WebdriverManager getWebdriverManager() {
        return webdriverManager;
    }

    private ReportService getReportService() {
        if (reportService == null) {
            reportService = new ReportService(getOutputDirectory(), getDefaultReporters());
        }
        return reportService;
    }

    /**
     * A test runner can generate reports via Reporter instances that subscribe
     * to the test runner. The test runner tells the reporter what directory to
     * place the reports in. Then, at the end of the test, the test runner
     * notifies these reporters of the test outcomes. The reporter's job is to
     * process each test run outcome and do whatever is appropriate.
     *
     * @param testOutcomeResults the test results from the previous test run.
     */
    private void generateReportsFor(final List<TestOutcome> testOutcomeResults) {
        getReportService().generateReportsFor(testOutcomeResults);
        getReportService().generateConfigurationsReport();
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {

        TestMethodConfiguration theMethod = TestMethodConfiguration.forMethod(method);

        clearMetadataIfRequired();

        if (shouldSkipTest(method)) {
            return;
        }

        if (theMethod.isManual()) {
            markAsManual(method);
            notifier.fireTestIgnored(describeChild(method));
            return;
        } else if (theMethod.isPending()) {
            markAsPending(method);
            notifier.fireTestIgnored(describeChild(method));
            return;
        } else {
            processTestMethodAnnotationsFor(method);
        }

        FailureDetectingStepListener failureDetectingStepListener = new FailureDetectingStepListener();
        StepEventBus.getEventBus().registerListener(failureDetectingStepListener);

        for (int attemptCount = 0; attemptCount < theTest.getMaxRetries() + 1; attemptCount += 1) {
            if (notifier instanceof RetryFilteringRunNotifier) {
                ((RetryFilteringRunNotifier) notifier).reset();
            }
            if (attemptCount > 0) {
                logger.warn("{} failed, making attempt number {} out of 1 base call + {} retries",
                            method.getName(), attemptCount, theTest.getMaxRetries());
                StepEventBus.getEventBus().testRetried();
            }

            initializeTestSession();
            prepareBrowserForTest();
            additionalBrowserCleanup();
            failureDetectingStepListener.reset();

            super.runChild(method, notifier);

            if (!failureDetectingStepListener.lastTestFailed()) {
                break;
            }
        }

        if (notifier instanceof RetryFilteringRunNotifier) {
            ((RetryFilteringRunNotifier) notifier).flush();
        }
    }

    private void clearMetadataIfRequired() {
        if (theTest.shouldClearMetadata()) {
            Serenity.getCurrentSession().clearMetaData();
        }
    }

    protected void additionalBrowserCleanup() {
        // Template method. Override this to do additional cleanup e.g. killing IE processes.
    }

    private boolean shouldSkipTest(FrameworkMethod method) {
        return !tagScanner.shouldRunMethod(getTestClass().getJavaClass(), method.getName());
    }

    private void markAsPending(FrameworkMethod method) {
        testStarted(method);
        StepEventBus.getEventBus().testPending();
        StepEventBus.getEventBus().testFinished();
    }

    private void markAsManual(FrameworkMethod method) {
        testStarted(method);
        StepEventBus.getEventBus().testIsManual();
        StepEventBus.getEventBus().testFinished();
    }

    private void testStarted(FrameworkMethod method) {
        getStepListener().testStarted(Description.createTestDescription(method.getMethod().getDeclaringClass(), testName(method)));
    }

    /**
     * Process any Serenity annotations in the test class.
     * Ignored tests will just be skipped by JUnit - we need to ensure
     * that they are included in the Serenity reports
     * If a test method is pending, all the steps should be skipped.
     */
    private void processTestMethodAnnotationsFor(FrameworkMethod method) {
        if (isIgnored(method)) {
            testStarted(method);
            StepEventBus.getEventBus().testIgnored();
        }
    }

    protected void prepareBrowserForTest() {
        if (theTest.needsToRestartTheBrowser()) {
            WebdriverProxyFactory.resetDriver(getDriver());
        }

        if (theTest.shouldClearTheBrowserSession()) {
            WebdriverProxyFactory.clearBrowserSession(getDriver());
        }
    }

    /**
     * Running a unit test, which represents a test scenario.
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {

        if (webtestsAreSupported()) {
            injectDriverInto(test);
            initPagesObjectUsing(driverFor(method));
            injectAnnotatedPagesObjectInto(test);
            initStepFactoryUsing(getPages());
        }

        injectScenarioStepsInto(test);
        injectEnvironmentVariablesInto(test);
        useStepFactoryForDataDrivenSteps();

        Statement baseStatement = super.methodInvoker(method, test);
        return new SerenityStatement(baseStatement, stepListener.getBaseStepListener());
    }

    private void useStepFactoryForDataDrivenSteps() {
        StepData.setDefaultStepFactory(stepFactory);
    }

    /**
     * Instantiate the @Managed-annotated WebDriver instance with current WebDriver.
     * @param testCase A Serenity-annotated test class
     */
    protected void injectDriverInto(final Object testCase) {
        TestCaseAnnotations.forTestCase(testCase).injectDrivers(getWebdriverManager());
        dependencyInjector.injectDependenciesInto(testCase);
    }

    protected WebDriver driverFor(final FrameworkMethod method) {
        if (TestMethodAnnotations.forTest(method).isDriverSpecified()) {
            String testSpecificDriver = TestMethodAnnotations.forTest(method).specifiedDriver();
            return getDriver(testSpecificDriver);
        } else {
            return getDriver();
        }
    }

    /**
     * Instantiates the @ManagedPages-annotated Pages instance using current WebDriver.
     * @param testCase A Serenity-annotated test class
     */
    protected void injectScenarioStepsInto(final Object testCase) {
        StepAnnotations.injectScenarioStepsInto(testCase, stepFactory);
    }

    /**
     * Instantiates the @ManagedPages-annotated Pages instance using current WebDriver.
     * @param testCase A Serenity-annotated test class
         */
    protected void injectAnnotatedPagesObjectInto(final Object testCase) {
        StepAnnotations.injectAnnotatedPagesObjectInto(testCase, pages);
    }

    protected void injectEnvironmentVariablesInto(final Object testCase) {
        EnvironmentDependencyInjector environmentDependencyInjector = new EnvironmentDependencyInjector();
        environmentDependencyInjector.injectDependenciesInto(testCase);
    }

    protected WebDriver getDriver() {
        return getWebdriverManager().getWebdriver(requestedDriver);
    }

    protected WebDriver getDriver(final String driver) {
        return getWebdriverManager().getWebdriver(driver);
    }

    /**
     * Find the current set of test outcomes produced by the test execution.
     * @return the current list of test outcomes
     */
    public List<TestOutcome> getTestOutcomes() {
        return getStepListener().getTestOutcomes();
    }

    /**
     *  @return The default reporters applicable for standard test runs.
     */
    protected Collection<AcceptanceTestReporter> getDefaultReporters() {
        return ReportService.getDefaultReporters();
    }
}
