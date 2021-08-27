package co.com.julianr0223.starter;

import io.reactivex.Single;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.http.HttpServer;
import io.vertx.reactivex.ext.web.Router;

public class MainVerticle extends AbstractVerticle {

  private Logger logger;
  private int port;
  private HSMWebClient HSMWebclient;

  private final String apen = "----------->>>>";
  private final String prepen = "<<<-----------";

  public MainVerticle() {
    this.logger = LoggerFactory.getLogger(MainVerticle.class);;
    this.port = 8082;
  }

  @Override
  public void start(Promise<Void> startPromise) {
    var router = Router.router(vertx);
    this.HSMWebclient = new HSMWebClient("http://vgowj.mocklab.io", 80, vertx);

    router.get("/keypair/:keyId")
      .blockingHandler(event -> {
        logger.info(apen + "Block Handler" + prepen);

        String keyId = event.pathParam("keyId");

        Single<KeyPairDTO> future = HSMWebclient.get(keyId);

        future.subscribe(keyPairDTO -> {
          System.out.println(keyPairDTO);
          event.end(keyPairDTO.toString());
        });
      });


    HttpServer httpServer = vertx.createHttpServer();
    httpServer
      .requestHandler(router)
      .listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        logger.error("HTTP server started on port " + port);
      } else {
        startPromise.fail(http.cause());
      }
    });
  }
}
