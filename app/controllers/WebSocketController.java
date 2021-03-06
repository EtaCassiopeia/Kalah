package controllers;

import actors.SupervisorActor;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.Materializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F.Either;
import play.mvc.*;
import scala.compat.java8.FutureConverters;
import securesocial.core.RuntimeEnvironment;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;

/**
 * WebSocketController class is responsible for creating a WebSocket connection for each player.
 * It will ask the {@see actors.SupervisorActor} to create a new child actor per player.
 */
@Singleton
public class WebSocketController extends Controller {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    private final ActorRef supervisorActor;
    private final Materializer materializer;
    private final ActorSystem actorSystem;
    private RuntimeEnvironment env;

    @Inject
    public WebSocketController(RuntimeEnvironment env, ActorSystem actorSystem,
                               Materializer materializer,
                               @Named("supervisorActor") ActorRef supervisorActor) {
        this.supervisorActor = supervisorActor;
        this.materializer = materializer;
        this.actorSystem = actorSystem;
        this.env = env;
    }

    public WebSocket ws(String playerName) {
        return WebSocket.Json.acceptOrResult(request -> {
            final CompletionStage<Flow<JsonNode, JsonNode, NotUsed>> future = wsFuture(request, playerName);
            final CompletionStage<Either<Result, Flow<JsonNode, JsonNode, ?>>> stage = future.thenApplyAsync(Either::Right);
            return stage.exceptionally(this::logException);
        });
    }

    public Either<Result, Flow<JsonNode, JsonNode, ?>> logException(Throwable throwable) {
        logger.error("Cannot create websocket", throwable);
        Result result = Results.internalServerError("error");
        return Either.Left(result);
    }

    public CompletionStage<Flow<JsonNode, JsonNode, NotUsed>> wsFuture(Http.RequestHeader request, String playerName) {
        final Pair<ActorRef, Publisher<JsonNode>> pair = createWebSocketConnections();
        ActorRef webSocketOut = pair.first();
        Publisher<JsonNode> webSocketIn = pair.second();

        final CompletionStage<ActorRef> userActorFuture = createUserActor(playerName, webSocketOut);

        final CompletionStage<Flow<JsonNode, JsonNode, NotUsed>> stage = userActorFuture
                .thenApplyAsync(userActor -> createWebSocketFlow(webSocketIn, userActor));

        return stage;
    }

    public CompletionStage<ActorRef> createUserActor(String id, ActorRef webSocketOut) {
        long timeoutMillis = 100L;
        return FutureConverters.toJava(
                ask(supervisorActor, new SupervisorActor.Create(id, webSocketOut), timeoutMillis)
        ).thenApply(stageObj -> (ActorRef) stageObj);
    }

    public Pair<ActorRef, Publisher<JsonNode>> createWebSocketConnections() {
        final Source<JsonNode, ActorRef> source = Source.actorRef(10, OverflowStrategy.dropTail());

        final Sink<JsonNode, Publisher<JsonNode>> sink = Sink.asPublisher(AsPublisher.WITHOUT_FANOUT);

        return source.toMat(sink, Keep.both()).run(materializer);
    }

    public Flow<JsonNode, JsonNode, NotUsed> createWebSocketFlow(Publisher<JsonNode> webSocketIn, ActorRef userActor) {
        final Sink<JsonNode, NotUsed> sink = Sink.actorRef(userActor, new akka.actor.Status.Success("success"));
        final Source<JsonNode, NotUsed> source = Source.fromPublisher(webSocketIn);
        final Flow<JsonNode, JsonNode, NotUsed> flow = Flow.fromSinkAndSource(sink, source);

        return flow.watchTermination((ignore, termination) -> {
            termination.whenComplete((done, throwable) -> {
                logger.info("Terminating actor {}", userActor);
                actorSystem.stop(userActor);
            });

            return NotUsed.getInstance();
        });
    }
}

