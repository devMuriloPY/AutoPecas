package Service;

import java.io.IOException;
import java.util.Map;

/** Consulta de empresa pelo CNPJ na API gratuita BrasilAPI (sem cadastro e sem token). */
public final class ConsultaCnpj {

    public record Empresa(String razaoSocial, String nomeFantasia, String cep, String logradouro, String numero,
            String bairro, String cidade, String uf, String telefone, String email) {
    }

    private ConsultaCnpj() {
    }

    /**
     * @param cnpj CNPJ com ou sem máscara
     * @return os dados da empresa, ou null se o CNPJ não for encontrado
     * @throws IllegalArgumentException se o CNPJ não tiver 14 dígitos
     * @throws IOException se o serviço estiver fora do ar, sem internet, ou recusar o CNPJ
     */
    public static Empresa buscar(String cnpj) throws IOException, InterruptedException {
        String digitos = Texto.soDigitos(cnpj);
        if (digitos.length() != 14) {
            throw new IllegalArgumentException("O CNPJ deve ter 14 dígitos");
        }

        ApiHttp.Resposta r = ApiHttp.get("https://brasilapi.com.br/api/cnpj/v1/" + digitos);
        if (r.status() == 404) {
            return null;
        }
        Map<String, String> json = JsonSimples.lerObjeto(r.corpo());
        if (r.status() != 200) {
            String mensagem = json.get("message");
            throw new IOException(mensagem != null ? mensagem : "O serviço de CNPJ respondeu com erro HTTP " + r.status());
        }

        // "AVENIDA" + "BRIG FARIA LIMA" -> "AVENIDA BRIG FARIA LIMA"
        String tipo = json.get("descricao_tipo_de_logradouro");
        String rua = json.get("logradouro");
        String logradouro = ((tipo == null ? "" : tipo.trim()) + " " + (rua == null ? "" : rua.trim())).trim();

        return new Empresa(json.get("razao_social"), json.get("nome_fantasia"), Texto.soDigitos(json.get("cep")),
                logradouro, json.get("numero"), json.get("bairro"), json.get("municipio"), json.get("uf"),
                Texto.soDigitos(json.get("ddd_telefone_1")), json.get("email"));
    }
}
