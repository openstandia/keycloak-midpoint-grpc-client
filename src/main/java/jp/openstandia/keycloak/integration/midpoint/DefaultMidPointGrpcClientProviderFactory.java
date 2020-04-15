package jp.openstandia.keycloak.integration.midpoint;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.concurrent.TimeUnit;

public class DefaultMidPointGrpcClientProviderFactory implements MidPointGrpcClientProviderFactory {

    private static final Logger logger = Logger.getLogger(DefaultMidPointGrpcClientProviderFactory.class);

    // channel is thread-safe
    private static ManagedChannel channel;
    private String clientId;
    private String clientSecret;

    protected Config.Scope scope;

    private final Object lock = new Object();

    @Override
    public MidPointGrpcClientProvider create(KeycloakSession session) {
        return new DefaultMidPointGrpcClientProvider(channel, clientId, clientSecret);
    }

    @Override
    public void init(Config.Scope scope) {
        logger.info("Initializing midpoint-grpc-client");

        this.scope = scope;

        String server = scope.get("server");
        int port = scope.getInt("port", 6565);

        // TODO should use own thread pool? Default is unlimited threads.
        channel = ManagedChannelBuilder.forAddress(server, port)
                .usePlaintext()
                .build();

        String clientId = scope.get("client-id");
        String clientSecret = scope.get("client-secret");

        logger.info("Initialized midpoint-grpc-client");
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
        logger.infov("Stopping midPoint gRPC client");

        if (channel != null) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("Interrupted while shutdown process", e);
            }
        }

        logger.infov("Stopped midPoint gRPC client");
    }

    @Override
    public String getId() {
        return "default";
    }
}
