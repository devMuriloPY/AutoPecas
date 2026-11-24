package Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Requisição GET simples (HttpClient do próprio Java, sem bibliotecas externas). */
final class ApiHttp {

    record Resposta(int status, String corpo) {
    }

    private static final HttpClient CLIENTE = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    private ApiHttp() {
    }

    static Resposta get(String url) throws IOException, InterruptedException {
        HttpRequest requisicao = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            return enviar(requisicao);
        } catch (IOException primeiraFalha) {
            // Quedas de conexão passageiras acontecem: uma nova tentativa resolve a maioria.
            Thread.sleep(500);
            return enviar(requisicao);
        }
    }

    private static Resposta enviar(HttpRequest requisicao) throws IOException, InterruptedException {
        HttpResponse<String> resposta = CLIENTE.send(requisicao, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new Resposta(resposta.statusCode(), resposta.body());
    }
}
