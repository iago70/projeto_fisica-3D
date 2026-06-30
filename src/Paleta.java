import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de cores e fontes usada em toda a aplicação, para manter a
 * identidade visual consistente entre os painéis.
 */
public final class Paleta {

    private Paleta() {
    }

    public static final Color FUNDO_JANELA   = new Color(0xF0, 0xF1, 0xF4);
    public static final Color FUNDO_CABECALHO = new Color(0x12, 0x2B, 0x5C);
    public static final Color FUNDO_CENA      = new Color(0xFB, 0xFC, 0xFD);

    public static final Color AZUL_PRINCIPAL  = new Color(0x1E, 0x3C, 0x82);
    public static final Color AZUL_CLARO      = new Color(0x3B, 0x6E, 0xD6);
    public static final Color VERDE           = new Color(0x27, 0xAE, 0x60);
    public static final Color VERDE_ESCURO    = new Color(0x1E, 0x8C, 0x4B);
    public static final Color VERMELHO        = new Color(0xE7, 0x4C, 0x3C);
    public static final Color VERMELHO_ESCURO = new Color(0xB8, 0x3A, 0x2E);

    public static final Color CARGA_POSITIVA_CLARO = new Color(0xFF, 0x8A, 0x75);
    public static final Color CARGA_POSITIVA_ESCURO = new Color(0xB9, 0x2A, 0x1B);
    public static final Color PONTO_C_CLARO   = new Color(0x6F, 0x9C, 0xFF);
    public static final Color PONTO_C_ESCURO  = new Color(0x12, 0x2B, 0x5C);

    public static final Color EIXO_X = new Color(0xE0, 0x43, 0x3F);
    public static final Color EIXO_Y = new Color(0x2E, 0xA8, 0x4F);
    public static final Color EIXO_Z = new Color(0x2F, 0x6F, 0xE0);

    public static final Color CARTAO_FUNDO    = Color.WHITE;
    public static final Color CARTAO_BORDA    = new Color(0xDD, 0xE1, 0xE8);
    public static final Color RESULTADO_FUNDO = new Color(0xFE, 0xF8, 0xDE);
    public static final Color RESULTADO_BORDA = new Color(0xF0, 0xE2, 0xA0);

    public static final Color TEXTO_PRIMARIO   = new Color(0x22, 0x28, 0x33);
    public static final Color TEXTO_SECUNDARIO = new Color(0x6B, 0x74, 0x82);

    public static final Font FONTE_TITULO      = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONTE_SUBTITULO   = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONTE_LABEL       = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONTE_CAMPO       = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONTE_RESULTADO   = new Font("Monospaced", Font.BOLD, 15);
    public static final Font FONTE_INFO        = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONTE_BOTAO       = new Font("SansSerif", Font.BOLD, 13);
}
