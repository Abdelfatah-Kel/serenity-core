package net.thucydides.model.reports.adaptors;

import net.serenitybdd.model.collect.NewMap;
import net.serenitybdd.model.environment.EnvironmentSpecificConfiguration;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.reports.adaptors.lettuce.LettuceXUnitAdaptor;
import net.thucydides.model.reports.adaptors.specflow.SpecflowAdaptor;
import net.thucydides.model.reports.adaptors.xunit.DefaultXUnitAdaptor;
import net.thucydides.model.util.EnvironmentVariables;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class AdaptorService {

    private final static Map<String, ? extends TestOutcomeAdaptor> BUILT_IN_ADAPTORS
            = NewMap.of("xunit", new DefaultXUnitAdaptor(),
                              "specflow", new SpecflowAdaptor(),
                              "lettuce", new LettuceXUnitAdaptor());

    private final EnvironmentVariables environmentVariables;

    public AdaptorService(EnvironmentVariables environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public AdaptorService() {
        this(SystemEnvironmentVariables.currentEnvironmentVariables() );
    }

    public TestOutcomeAdaptor getAdaptor(String name) {
        if (BUILT_IN_ADAPTORS.containsKey(name)) {
            return BUILT_IN_ADAPTORS.get(name);
        }
//        String customAdaptor = environmentVariables.getProperty("serenity.adaptors." + name);

        String customAdaptor = EnvironmentSpecificConfiguration.from(environmentVariables)
                .getOptionalProperty("serenity.adaptors." + name)
                .orElse(
                        EnvironmentSpecificConfiguration.from(environmentVariables)
                                                        .getOptionalProperty("thucydides.adaptors." + name)
                                                        .orElse(null)
                );


//        if (customAdaptor == null) {
//            customAdaptor = environmentVariables.getProperty("thucydides.adaptors." + name);
//        }
        if (StringUtils.isNotEmpty(customAdaptor)) {
            return newAdaptor(customAdaptor);
        }
        throw new UnknownAdaptor("Unknown test outcome adaptor: " + name);
    }

    private TestOutcomeAdaptor newAdaptor(String customAdaptor) {
        try {
            return (TestOutcomeAdaptor) Class.forName(customAdaptor).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new UnknownAdaptor("Test outcome adaptor could not be instanciated: " + customAdaptor, e);
        }
    }
}
