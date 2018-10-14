package net.serenitybdd.core.reports;

import net.thucydides.core.model.ReportData;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.steps.StepEventBus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;

import static net.thucydides.core.ThucydidesSystemProperty.SERENITY_REPORT_ENCODING;

public class ReportDataSaver implements WithTitle, AndContent, FromFile {


    private final StepEventBus eventBus;
    private String title;
    private boolean fileIsDownloadable = false;
    private boolean isEvidence = false;

    public ReportDataSaver(StepEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public AndContent withTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public void andContents(String contents) {
        eventBus.getBaseStepListener().latestTestOutcome().ifPresent(
                outcome -> outcome.currentStep().withReportData(ReportData.withTitle(title).andContents(contents).asEvidence(isEvidence))
        );
    }

    @Override
    public void fromFile(Path source) throws IOException {
        Charset encoding = Charset.forName(SERENITY_REPORT_ENCODING.from(eventBus.getEnvironmentVariables(), StandardCharsets.UTF_8.name()));
        fromFile(source, encoding);
    }

    @Override
    public void fromFile(Path source, Charset encoding) throws IOException {

        Optional<TestOutcome> outcome = eventBus.getBaseStepListener().latestTestOutcome();

        if (outcome.isPresent()) {
            ReportData reportData = (fileIsDownloadable) ?
                    ReportData.withTitle(title).fromPath(source).asEvidence(isEvidence) :
                    ReportData.withTitle(title).fromFile(source, encoding).asEvidence(isEvidence);

            outcome.get().currentStep().withReportData(reportData);
        }
    }

    @Override
    public FromFile downloadable() {
        this.fileIsDownloadable = true;
        return this;
    }

    @Override
    public FromFile asEvidence() {
        this.isEvidence = true;
        return this;
    }
}
