package customer.incident_management.mocked;

import com.sap.cloud.sdk.cloudplatform.connectivity.AuthenticationType;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
@Profile("mocked")
public class MockDestinationConfiguration {

    private final Environment environment;

    @EventListener
    void applicationReady(ApplicationReadyEvent ready) {

        int port =
                Integer.parseInt(Objects.requireNonNull(environment.getProperty("local.server.port")));

        DefaultHttpDestination destination =
                DefaultHttpDestination.builder("http://localhost:" + port + "/odata/v4")
                        .name("mocked-remote-business-partner-destination")
                        .authenticationType(AuthenticationType.BASIC_AUTHENTICATION)
                        .basicCredentials("alice", "")
                        .build();

        DefaultDestinationLoader loader = new DefaultDestinationLoader();
        loader.registerDestination(destination);
        DestinationAccessor.prependDestinationLoader(loader);
        log.info(
                "Successfully configured Mocked BUPA Destination...{}",
                destination.getUri());
    }

}
