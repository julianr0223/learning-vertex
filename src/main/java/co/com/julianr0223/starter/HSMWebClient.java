package co.com.julianr0223.starter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.util.internal.ObjectUtil;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class HSMWebClient {
  private String host;
  private int port;
  private WebClient webClient;
  private Cache<String, KeyPairDTO> cache;

  public HSMWebClient(String host, int port, Vertx vertx) {
    this.host = host;
    this.port = port;

//    WebClientOptions webClientOptions = new WebClientOptions()
//      .setSsl(false)
//      .setTrustAll(true);
//    this.webClient = WebClient.create(vertx, webClientOptions);
    this.webClient = WebClient.create(vertx);

    cache = Caffeine.newBuilder()
                    .expireAfterWrite(15, TimeUnit.SECONDS).build();
  }

  public Single<KeyPairDTO> get(String keyPairId) {
    KeyPairDTO keyPairCached = cache.getIfPresent(keyPairId);

    if(Objects.nonNull(keyPairCached)) {
      System.out.println("Cacheado para " + keyPairId);
      return Single.just(keyPairCached);
    }

    return getMock(keyPairId)
            .doOnSuccess(keyPairDTO -> cache.put(keyPairId, keyPairDTO));
  }

  private Single<KeyPairDTO> getMockHSMAPI(String keyPairId) {
    return this.webClient
      .get(port, host, "/keys/" + keyPairId)
      .rxSend()
      .map(bufferHttpResponse -> {
        var entries = bufferHttpResponse.bodyAsJsonObject();
        return new KeyPairDTO(entries.getString("privateKey"), entries.getString("publicKey"));
      });
  }

  private Single<KeyPairDTO> getMock(String keyPairId) {
    var keyPair = new KeyPairDTO("PRIVATE->" + keyPairId, "PUBLIC->" + keyPairId);
    return Single.just(keyPair)
      .delay(2, TimeUnit.SECONDS);
  }


}

class KeyPairDTO {

  private final String privateKey;
  private final String publicKey;

  public KeyPairDTO(String privateKey, String publicKey) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  @Override
  public String toString() {
    return "HahCode " + hashCode() + " KeyPair{" +
      "privateKey='" + privateKey + '\'' +
      ", publicKey='" + publicKey + '\'' +
      '}';
  }
}
