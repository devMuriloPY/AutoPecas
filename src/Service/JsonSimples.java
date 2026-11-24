package Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Leitor mínimo de JSON, suficiente para respostas de APIs simples (ViaCEP, BrasilAPI).
 *
 * Lê um objeto JSON e devolve os seus campos de primeiro nível como texto.
 * Valores "null" viram null; objetos e listas aninhados são ignorados.
 */
public final class JsonSimples {

    private final String s;
    private int i = 0;

    private JsonSimples(String json) {
        this.s = json;
    }

    public static Map<String, String> lerObjeto(String json) {
        return new JsonSimples(json).objeto();
    }

    private Map<String, String> objeto() {
        Map<String, String> campos = new HashMap<>();
        pulaEspacos();
        esperado('{');
        pulaEspacos();
        if (atual() == '}') {
            return campos;
        }
        while (true) {
            pulaEspacos();
            String chave = texto();
            pulaEspacos();
            esperado(':');
            pulaEspacos();
            char c = atual();
            if (c == '"') {
                campos.put(chave, texto());
            } else if (c == '{' || c == '[') {
                pulaComposto();
            } else {
                String literal = literal();
                campos.put(chave, "null".equals(literal) ? null : literal);
            }
            pulaEspacos();
            if (atual() == ',') {
                i++;
                continue;
            }
            esperado('}');
            return campos;
        }
    }

    private char atual() {
        if (i >= s.length()) {
            throw new IllegalArgumentException("JSON incompleto");
        }
        return s.charAt(i);
    }

    private void esperado(char c) {
        if (atual() != c) {
            throw new IllegalArgumentException("JSON inválido: esperado '" + c + "' na posição " + i);
        }
        i++;
    }

    private void pulaEspacos() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    private String literal() {
        int ini = i;
        while (i < s.length() && ",}] \t\r\n".indexOf(s.charAt(i)) < 0) {
            i++;
        }
        return s.substring(ini, i);
    }

    private String texto() {
        esperado('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            char c = atual();
            i++;
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            char e = atual();
            i++;
            switch (e) {
                case 'n' -> sb.append('\n');
                case 't' -> sb.append('\t');
                case 'r' -> sb.append('\r');
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'u' -> {
                    sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                }
                default -> sb.append(e); // \" \\ \/
            }
        }
    }

    /** Pula um objeto ou lista inteiro, respeitando textos entre aspas. */
    private void pulaComposto() {
        int nivel = 0;
        do {
            char c = atual();
            if (c == '"') {
                texto();
                continue;
            }
            if (c == '{' || c == '[') {
                nivel++;
            } else if (c == '}' || c == ']') {
                nivel--;
            }
            i++;
        } while (nivel > 0);
    }
}
