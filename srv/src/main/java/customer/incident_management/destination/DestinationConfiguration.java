package customer.incident_management.destination;

import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultDestinationLoader;
import com.sap.cloud.sdk.cloudplatform.connectivity.DefaultHttpDestination;
import com.sap.cloud.sdk.cloudplatform.connectivity.DestinationAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
@Profile({"default","sandbox","cloud"})
public class DestinationConfiguration {

    @EventListener
    void applicationReady(ApplicationReadyEvent ready) {
        DefaultHttpDestination destination =
                DefaultHttpDestination.builder("https://sandbox.api.sap.com")
                        .name("API_BUSINESS_PARTNER")
                        .build();

        DefaultDestinationLoader loader = new DefaultDestinationLoader();
        loader.registerDestination(destination);
        DestinationAccessor.prependDestinationLoader(loader);
        log.info(
                "Successfully configured BUPA Destination...{}",
                destination.getUri());
    }

}
