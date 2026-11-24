package Service;

import java.io.IOException;
import java.util.Map;

/** Consulta de endereço pelo CEP na API gratuita ViaCEP (sem cadastro e sem token). */
public final class ConsultaCep {

    public record Endereco(String cep, String logradouro, String bairro, String cidade, String uf) {
    }

    private ConsultaCep() {
    }

    /**
     * @param cep CEP com ou sem máscara
     * @return o endereço, ou null se o CEP não existir
     * @throws IllegalArgumentException se o CEP não tiver 8 dígitos
     * @throws IOException se o serviço estiver fora do ar ou sem internet
     */
    public static Endereco buscar(String cep) throws IOException, InterruptedException {
        String digitos = Texto.soDigitos(cep);
        if (digitos.length() != 8) {
            throw new IllegalArgumentException("O CEP deve ter 8 dígitos");
        }

        ApiHttp.Resposta r = ApiHttp.get("https://viacep.com.br/ws/" + digitos + "/json/");
        if (r.status() != 200) {
            throw new IOException("O serviço de CEP respondeu com erro HTTP " + r.status());
        }

        Map<String, String> json = JsonSimples.lerObjeto(r.corpo());
        if (json.containsKey("erro")) {
            return null; // CEP com formato válido, mas inexistente
        }
        return new Endereco(Texto.soDigitos(json.get("cep")), json.get("logradouro"), json.get("bairro"),
                json.get("localidade"), json.get("uf"));
    }
}
