package Service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/** Prepara fotos de produto para guardar no banco: reduz o tamanho e converte para JPEG. */
public final class Imagem {

    /** Maior lado da foto guardada, em pixels. Suficiente para o PDV e mantém o banco leve. */
    public static final int LADO_MAXIMO = 900;

    private Imagem() {
    }

    /** @throws IOException se o arquivo não for uma imagem que o Java consiga abrir */
    public static BufferedImage ler(File arquivo) throws IOException {
        BufferedImage img = ImageIO.read(arquivo);
        if (img == null) {
            throw new IOException("O arquivo escolhido não é uma imagem JPG ou PNG válida");
        }
        return img;
    }

    /** @return a imagem, ou null se não houver bytes ou eles não formarem uma imagem */
    public static BufferedImage ler(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException ex) {
            return null;
        }
    }

    /** Reduz para caber em LADO_MAXIMO e troca transparência por fundo branco (JPEG não tem transparência). */
    public static BufferedImage normaliza(BufferedImage original) {
        double escala = Math.min(1.0, (double) LADO_MAXIMO / Math.max(original.getWidth(), original.getHeight()));
        int w = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int h = Math.max(1, (int) Math.round(original.getHeight() * escala));

        // reduz pela metade em passos: fica bem mais nítido do que reduzir de uma vez
        BufferedImage atual = original;
        int cw = original.getWidth(), ch = original.getHeight();
        while (cw / 2 >= w && ch / 2 >= h) {
            cw /= 2;
            ch /= 2;
            atual = desenha(atual, cw, ch);
        }
        return desenha(atual, w, h);
    }

    private static BufferedImage desenha(BufferedImage origem, int w, int h) {
        BufferedImage destino = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = destino.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(origem, 0, 0, w, h, null);
        g.dispose();
        return destino;
    }

    public static byte[] paraJpeg(BufferedImage imagem) throws IOException {
        ImageWriter escritor = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam parametros = escritor.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(0.86f);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(saida)) {
            escritor.setOutput(ios);
            escritor.write(null, new IIOImage(imagem, null, null), parametros);
        } finally {
            escritor.dispose();
        }
        return saida.toByteArray();
    }
}
